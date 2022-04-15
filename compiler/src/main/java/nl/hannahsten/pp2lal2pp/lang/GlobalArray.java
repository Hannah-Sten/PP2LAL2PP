package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;
import nl.hannahsten.pp2lal2pp.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Hannah Schellekens
 */
public class GlobalArray implements Element, Identifyable {

    /**
     * The unique id of the global array.
     */
    private final int id;

    /**
     * Contains all the variables that are stored in this array.
     */
    private final List<GlobalVariable> variables;

    /**
     * The name of the array, all child variables have the same name.
     */
    private final String name;

    /**
     * The default value of the variable.
     */
    private Value defaultValue = Number.ZERO;

    /**
     * Creates a global array of {@code size} variables.
     */
    public static GlobalArray withSize(String name, int size, Comment lastComment) {
        List<GlobalVariable> variables = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            variables.add(new GlobalVariable(name, lastComment));
        }
        return new GlobalArray(name, variables);
    }

    public GlobalArray(String name, GlobalVariable... variables) {
        this(name, Arrays.asList(variables));
    }

    public GlobalArray(String name, List<GlobalVariable> variables) {
        if (variables.isEmpty()) {
            throw new IllegalArgumentException("Global array must have at least 1 variable.");
        }

        this.id = IDManager.newId();
        this.name = name;
        this.variables = Collections.unmodifiableList(variables);
    }

    public GlobalVariable get(int index) {
        if (index < 0 || index >= variables.size()) {
            throw new ParseException("Index out of bounds: " + index + " (size: " + variables.size() + ")");
        }
        return variables.get(index);
    }

    public List<GlobalVariable> getVariables() {
        return Collections.unmodifiableList(variables);
    }

    /**
     * @return The pointer to the first variable in the array.
     */
    public int getPointer() {
        return variables.get(0).getPointer();
    }

    /**
     * Updates the pointers of the variables in the global array.
     *
     * @param firstPointer
     *          The new pointer of the first element in the array.
     */
    public void setPointers(int firstPointer) {
        int variableCount = size();
        for (int i = 0; i < variableCount; i++) {
            Variable variable = variables.get(i);
            variable.setPointer(firstPointer + i);
        }
    }

    /**
     * The amount of variables in the array.
     */
    public int size() {
        return variables.size();
    }

    public String getName() {
        return name;
    }

    public Number getDefaultNumber(Program program) {
        if (defaultValue instanceof NumberConstant) {
            return (Number)program.getDefinition(((NumberConstant)defaultValue).getName()).get().getValue();
        }
        if (defaultValue instanceof Number) {
            return (Number)defaultValue;
        }
        return null;
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Value defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Value getValue() {
        return Value.EMPTY;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(global[] = [ ");
        for (Variable variable : variables) {
            sb.append(variable.toString());
        }
        return sb.append(" ])").toString();
    }
}
