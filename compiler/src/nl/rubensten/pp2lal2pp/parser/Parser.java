package nl.rubensten.pp2lal2pp.parser;

import nl.rubensten.pp2lal2pp.ParseException;
import nl.rubensten.pp2lal2pp.lang.Program;

/**
 * Takes one big code string and parses it to a {@link Program}.
 * <p>
 * This class does not handle inclusions and definitions.
 *
 * @author Ruben Schellekens
 */
public class Parser {

    /**
     * The complete string of code to parse.
     */
    protected String input;

    public Parser(String input) {
        this.input = input;
    }

    /**
     * Parses the complete input.
     */
    public Program parse() throws ParseException {
        Program program = new Program();

        // TODO: Parse the input.

        return program;
    }

}
