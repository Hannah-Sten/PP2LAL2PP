package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.IDManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Ruben Schellekens
 */
public class Function implements Identifyable, Element {

    /**
     * Unique id of the function.
     */
    private int id;

    /**
     * The name of the function.
     */
    private String name;

    /**
     * A list of arguments with a pointer relative to the stack pointer at the beginning of the
     * function.
     * <p>
     * List is empty if there are no arguments.
     */
    private List<Variable> arguments;

    /**
     * The statements that have to be executed in the block.
     */
    private Block contents;

    /**
     * Automatically assigns pointers to the variables.
     */
    public Function(String name, Variable... arguments) {
        this.id = IDManager.newId();
        this.name = name;
        this.arguments = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            Variable var = arguments[i];
            var.setPointer(i + 1);
        }
    }

    /**
     * Automatically assigns pointers to the variables.
     */
    public Function(String name, List<Variable> arguments) {
        this.id = IDManager.newId();
        this.name = name;
        this.arguments = new ArrayList<>(arguments);

        for (int i = 0; i < arguments.size(); i++) {
            Variable var = arguments.get(i);
            var.setPointer(i + 1);
        }
    }

    /**
     * Finds the argument with the given name.
     *
     * @param name
     *         The name of the argument to look up.
     * @return The argument with the given name.
     */
    public Optional<Variable> getArgumentByName(String name) {
        for (Variable arg : arguments) {
            if (arg.getName().equals(name)) {
                return Optional.of(arg);
            }
        }

        return Optional.empty();
    }

    public String getName() {
        return name;
    }

    public void setContents(Block contents) {
        this.contents = contents;
    }

    public Block getContents() {
        return contents;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return new Value(Constants.REG_RETURN);
    }

    @Override
    public String toString() {
        return "Function{" + "arguments=" + arguments +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", contents=" + contents +
                '}';
    }
}
