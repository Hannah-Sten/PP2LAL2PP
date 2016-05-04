package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.IDManager;

import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class FunctionCall extends Value implements Element, Identifyable {

    /**
     * Unique id of the function call.
     */
    private int id;

    /**
     * The name of the function that is called.
     */
    private String called;

    /**
     * The list of arguments to call the function with.
     */
    private List<Variable> arguments;

    public FunctionCall(String called, List<Variable> arguments) {
        super(called);
        this.id = IDManager.newId();
        this.called = called;
        this.arguments = arguments;
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
