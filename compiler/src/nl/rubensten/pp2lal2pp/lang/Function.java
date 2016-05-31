package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.CompilerException;
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
     * A list of all local variables with pointers relative to the stack pointer.
     * <p>
     * List is empty if there are no variables.
     */
    private List<Variable> variables;

    /**
     * The statements that have to be executed in the block.
     */
    private Block contents;

    /**
     * The pp2doc documentation comment.
     * <p>
     * Every line is a new element int he list.
     */
    private List<String> pp2doc;

    /**
     * Automatically assigns pointers to the variables.
     */
    public Function(String name, List<String> pp2doc, Variable... arguments) {
        this.id = IDManager.newId();
        this.name = name;
        this.pp2doc = pp2doc;
        this.arguments = new ArrayList<>();
        this.variables = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            Variable var = arguments[i];
            var.setPointer(i + 1);
        }
    }

    /**
     * Automatically assigns pointers to the variables.
     */
    public Function(String name, List<String> pp2doc, List<Variable> arguments) {
        this.id = IDManager.newId();
        this.name = name;
        this.pp2doc = pp2doc;
        this.arguments = new ArrayList<>(arguments);
        this.variables = new ArrayList<>();

        for (int i = 0; i < arguments.size(); i++) {
            Variable var = arguments.get(i);
            var.setPointer(i + 1);
        }
    }

    /**
     * Returns the amount of local variables.
     */
    public int variableCount() {
        return variables.size();
    }

    /**
     * Declares a new local variable.
     *
     * @param variable
     *         The variable to declare.
     * @throws CompilerException
     *         if there already is a variable declared with the same name.
     */
    public void declareLocal(Variable variable) throws CompilerException {
        if (variables.parallelStream().anyMatch(v -> v.getName().equals(variable.getName()))) {
            throw new CompilerException("you can't declare " + variable.getName() + " twice");
        }

        variable.setPointer(newPointer());
        variables.add(variable);
    }

    /**
     * Unregisters a local variable. This will also reset the other pointers.
     *
     * @param variable
     *         The variable to deregister.
     * @throws CompilerException
     *         If the variable doesn't exist.
     */
    public void unregisterLocal(Variable variable) throws CompilerException {
        if (!variables.parallelStream().anyMatch(v -> v.getName().equals(variable.getName()))) {
            throw new CompilerException("there is no declared variable called '" + variable
                    .getName() + "'");
        }

        variables.removeIf(v -> v.getName().equals(variable.getName()));

        for (Variable var : variables) {
            var.setPointer(var.getPointer() - 1);
        }

        for (Variable arg : arguments) {
            arg.setPointer(arg.getPointer() - 1);
        }
    }

    /**
     * Looks what the last pointer used was and creates a new pointer value based on that.
     */
    private int newPointer() {
        for (Variable var : variables) {
            var.setPointer(var.getPointer() + 1);
        }

        for (Variable var : arguments) {
            var.setPointer(var.getPointer() + 1);
        }

        return 0;
    }

    /**
     * Finds the variable or argument object with the name of the given variable.
     *
     * @param other
     *         The variable from which the name will be used to get the variable with the same
     *         name.
     * @return The stored function-variable with the name of variable <code>other</code>.
     * @throws CompilerException
     *         if there is no variable declared with the name of the given variable.
     */
    public Variable getVariableByVariable(Variable other) throws CompilerException {
        return getVariableByName(other.getName());
    }

    /**
     * Finds the variable or argument object with the given name.
     *
     * @param name
     *         The name of the variable to look up.
     * @return The variable that carries the given name.
     * @throws CompilerException
     *         if there is no variable declared with the given name.
     */
    public Variable getVariableByName(String name) throws CompilerException {
        for (Variable var : variables) {
            if (var.getName().equals(name)) {
                return var;
            }
        }

        for (Variable arg : arguments) {
            if (arg.getName().equals(name)) {
                return arg;
            }
        }

        throw new CompilerException("variable " + name + " hasn't been declared");
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

    public List<String> getPp2doc() {
        return pp2doc;
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
                ", variables=" + variables +
                ", arguments=" + arguments +
                ", pp2doc=" + pp2doc +
                '}';
    }
}