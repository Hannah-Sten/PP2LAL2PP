package nl.rubensten.pp2lal2pp.util;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.PP2LAL2PPException;

/**
 * @author Ruben Schellekens
 */
public enum Template {

    API_EXIT("api-exit.template"),
    API_GETINPUTSTATES("api-getInputStates.template"),
    API_ISINPUTON("api-isInputOn.template"),
    API_SET7SEGMENT("api-set7Segment.template"),

    API_INVOKE_EXIT("api-invoke-exit.template"),
    API_INVOKE_GETINPUTSTATES("api-invoke-getInputStates.template"),
    API_INVOKE_ISINPUTON("api-invoke-isInputOn.template"),
    API_INVOKE_SET7SEGMENT("api-invoke-set7Segment.template"),

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
     * Calls {@link Template#replace(String...)} on {@link Template#STATEMENT} and automatically
     * determines the keys.
     *
     * @param label
     *         The label to put in front of the statement.
     * @param instruction
     *         The instruction.
     * @param arg1
     *         The first argument of the instruction.
     * @param arg2
     *         The second argument of the instruction.
     * @param cmt
     *         The comment without the ";"
     * @return A nicely formatted line of code.
     */
    public static String fillStatement(String label, String instruction, String arg1, String arg2,
                                       String cmt) {
        return Template.STATEMENT.replace(
                "LABEL", label,
                "INSTRUCTION", instruction,
                "ARG1", arg1,
                "ARG2", arg2)
                .replace("{$COMMENT}", "; " + cmt);
    }

    /**
     * Replaces all given keys with the given values.
     *
     * @param keyValue
     *         key value, key value, key value, ...
     * @return The string where all keys are replaced by their values.
     */
    public String replace(String... keyValue) {
        if (keyValue.length % 2 == 1) {
            throw new PP2LAL2PPException("keys don't match up with the values (odd length).");
        }

        String total = load();
        int overflow = 0;

        for (int i = 0; i < keyValue.length; i += 2) {
            int space = getSpace(total, keyValue[i]);
            int count = keyValue[i + 1].length();
            int tab = Math.max(1, space - count - overflow);
            overflow = 0;

            if (count + 1 > space) {
                overflow = -(space - (count + 1));
            }

            total = total.replace("{$" + keyValue[i], keyValue[i + 1])
                    .replaceFirst("%[0-9]+\\}", Util.makeString(" ", tab));
        }

        return total;
    }

    /**
     * Get the amount of characters of space is reserved for the given variable idk.
     */
    private int getSpace(String source, String name) {
        try {
            return Integer.parseInt(source.replaceAll(".*\\s*.*\\{\\$" + name + "%", "")
                    .replaceFirst("\\}.*\\s*.*", ""));
        }
        catch (NumberFormatException nfe) {
            throw new CompilerException("Template hasn't been set up correctly ({$" +
                    name + "%#}).");
        }
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

    public String getFilePath() {
        return filePath;
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
