package nl.rubensten.pp2lal2pp.util;

/**
 * @author Ruben Schellekens
 */
public enum Template {

    /**
     * Denotes the start of the program.
     */
    BEGIN_CODE("begin-code.template"),

    /**
     * The code to jump to the main function.
     */
    BEGIN_MAIN("begin-main.template"),

    /**
     * The default used EQU statements at the beginning of the program.
     */
    DEFAULT_EQU("default-equ.template"),

    /**
     * Denotes the end of the program.
     */
    END("end.template"),

    /**
     * Equals statement.
     * <p>
     * <b>Variables:</b>
     * <p>
     * <code>{$NAME%#}</code> The name of the variable filled up with spaces to reach # characters
     * in total length.
     * <p>
     * <code>{$VALUE%#}</code> The numerical value filled up with spaces to reach # characters.
     * <p>
     * <code>{$COMMENT}</code> The comment that must be placed after the statement.
     */
    EQU("equ.template"),

    /**
     * The Hex7Seg routine to load standard number patterns from 0-15.
     */
    HEX7SEG("hex7seg.template"),

    /**
     * An assembly statement/instruction.
     * <p>
     * <b>Variables:</b>
     * <p>
     * <code>{LABEL%#}</code> The name of the label filled up with spaces to reach # characters in
     * total length.
     * <p>
     * <code>{INSTRUCTION%#}</code> The name of the instruction filled up with spaces to reach #
     * characters in total length.
     * <p>
     * <code>{ARG1%#}</code> The name of the first argument of the instruction filled up with spaces
     * to reach # characters in total length.
     * <p>
     * <code>{ARG2%#}</code> The name of the second argument of the instruction filled up with
     * spaces to reach # characters in total length.
     * <p>
     * <code>{$COMMENT}</code> The comment that must be placed after the statement.
     */
    STATEMENT("statement.template");

    /**
     * The directory where the templates are stored followed by a slash.
     */
    private static String DIR = "/template/";

    /**
     * The file path relative to the program's root and {@link Template#DIR} where the template is
     * located.
     */
    private String filePath;

    Template(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads the contents of the template.
     *
     * @return The contents of the template.
     */
    public String load() {
        StreamWorker sw = new StreamWorker(getClass().getResourceAsStream(getPath()));
        return sw.read();
    }

    public String getPath() {
        return Template.DIR + filePath;
    }

    @Override
    /**
     * Calls {@link Template#load()}
     */
    public String toString() {
        return load();
    }
}
