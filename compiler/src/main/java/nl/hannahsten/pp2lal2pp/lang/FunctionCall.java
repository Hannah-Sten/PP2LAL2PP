package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

import java.util.List;

/**
 * @author Hannah Schellekens
 */
public class FunctionCall extends Value implements Element, Identifyable {

    /**
     * Unique id of the function call.
     */
    private final int id;

    /**
     * The name of the function that is called.
     */
    private final String called;

    /**
     * The list of arguments to call the function with.
     */
    private final List<Variable> arguments;

    public FunctionCall(String called, List<Variable> arguments) {
        super(called);
        this.id = IDManager.newId();
        this.called = called;
        this.arguments = arguments;
    }

    /**
     * Get a nicely formatted list of arguments.
     */
    public String getFormattedArguments() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        String sep = "";
        for (Variable var : arguments) {
            sb.append(sep);
            sb.append(var.getName());
            sep = ", ";
        }
        sb.append("]");

        return sb.toString();
    }

    public List<Variable> getArguments() {
        return arguments;
    }

    public String getCalled() {
        return called;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return new Value(called);
    }

    @Override
    public String toString() {
        return called + arguments;
    }

}
