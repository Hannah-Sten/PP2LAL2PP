package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.IDManager;

import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class FunctionCall implements Element, Identifyable {

    /**
     * Unique id of the function call.
     */
    private int id;

    /**
     * The function that is called.
     */
    private Function called;

    /**
     * The list of arguments to call the function with.
     */
    private List<Variable> arguments;

    public FunctionCall(Function called, List<Variable> arguments) {
        this.id = IDManager.newId();
        this.called = called;
        this.arguments = arguments;
    }

    public List<Variable> getArguments() {
        return arguments;
    }

    public Function getCalled() {
        return called;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return called.getValue();
    }

}
