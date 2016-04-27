package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.PP2LAL2PP;

import java.util.*;

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
    public Function(Variable... arguments) {
        this.id = PP2LAL2PP.globalId++;
        this.arguments = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            Variable var = arguments[i];
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

}
