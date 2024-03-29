package nl.hannahsten.pp2lal2pp.util;

import nl.hannahsten.pp2lal2pp.CompilerException;
import nl.hannahsten.pp2lal2pp.PP2LAL2PPException;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hannah Schellekens
 */
public enum Template {

    // API implementations
    API_ADDTIMER("api-addTimer.template"),
    API_EXIT("api-exit.template"),
    API_GETANALOG("api-getAnalog.template"),
    API_GETANALOGSTATES("api-getAnalogStates.template"),
    API_GETINPUTSTATES("api-getInputStates.template"),
    API_GETNUMPATTERN("api-getNumPattern.template"),
    API_GETPATTERN("api-getPattern.template"),
    API_GETTIMER("api-getTimer.template"),
    API_ISINPUTON("api-isInputOn.template"),
    API_SET7SEGMENT("api-set7Segment.template"),
    API_SETOUTPUT("api-setOutput.template"),
    API_SETSINGLEOUTPUT("api-setSingleOutput.template"),
    API_SETTIMER("api-setTimer.template"),

    // API function calls
    API_INVOKE_ADDTIMER("api-invoke-addTimer.template"),
    API_INVOKE_EXIT("api-invoke-exit.template"),
    API_INVOKE_GETANALOG("api-invoke-getAnalog.template"),
    API_INVOKE_GETANALOGSTATES("api-invoke-getAnalogStates.template"),
    API_INVOKE_GETINPUTSTATES("api-invoke-getInputStates.template"),
    API_INVOKE_GETNUMPATTERN("api-invoke-getNumPattern.template"),
    API_INVOKE_GETPATTERN("api-invoke-getPattern.template"),
    API_INVOKE_GETTIMER("api-invoke-getTimer.template"),
    API_INVOKE_ISINPUTON("api-invoke-isInputOn.template"),
    API_INVOKE_SET7SEGMENT("api-invoke-set7Segment.template"),
    API_INVOKE_SETOUTPUT("api-invoke-setOutput.template"),
    API_INVOKE_SETSINGLEOUTPUT("api-invoke-setSingleOutput.template"),
    API_INVOKE_SETTIMER("api-invoke-setTimer.template"),

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
     * Template to disable an interrupt (semi-API)
     */
    DISABLE_INTERRUPT("disable-interrupt.template"),

    /**
     * Template to enable an interrupt (semi-API)
     */
    ENABLE_INTERRUPT("enable-interrupt.template"),

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
     * Interrupt install 'invoke' template
     * <p>
     * <b>Variables:</b>
     * <p>
     * <code>{$ISRNAME1}</code> The name of interrupt.
     * <p>
     * <code>{$ISRNAME2}</code> The name of interrupt.
     * <p>
     * <code>{$ISRNAME3}</code> The name of interrupt.
     * <p>
     * <code>{$ISRNAME4}</code> The name of interrupt.
     * <p>
     * <code>{$ISRNAME5}</code> The name of interrupt.
     */
    INTERRUPT_BOILERPLATE_AFTER("interrupt-boilerplate-after.template"),

    /**
     * Interrupt install 'execute' and interrupt disable template
     * <p>
     * <b>Variables:</b>
     * <p>
     * <code>{$ISRNAME1}</code> The name of interrupt.
     * <p>
     * <code>{$ISRNAME2}</code> The name of interrupt.
     * <p>
     * <code>{$ISRNAME3%#}</code> The name of interrupt.
     */
    INTERRUPT_BOILERPLATE_BEFORE("interrupt-boilerplate-before.template"),

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
    private static final String DIR = "/template/";

    /**
     * The file path relative to the program's root and {@link Template#DIR} where the template is
     * located.
     */
    private final String filePath;

    /**
     * The contents of the template.
     */
    private String contents;

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
     * @param comment
     *         The comment without the ";"
     * @return A nicely formatted line of code.
     */
    public static String fillStatement(String label, String instruction, String arg1, String arg2,
                                       String comment) {
        return Regex.replace("{$COMMENT}", Template.STATEMENT.replace(
                "LABEL", label,
                "INSTRUCTION", instruction,
                "ARG1", arg1,
                "ARG2", arg2),
                (comment != null ? "; " + comment : ""));
    }

    /**
     * Unpacks all the templates to the executing directory.
     *
     * @return The amount of succesfully unpacked templates.
     */
    public static int unpack() {
        new File("template").mkdir();
        int count = 0;

        for (Template template : values()) {
            String text = template.load();
            String path = template.getPath().replaceAll("^/", "");
            File duFile = new File(path);

            if (duFile.exists()) {
                continue;
            }

            FileWorker fw = new FileWorker(duFile);

            try {
                fw.write(text, false);
                count++;
            }
            catch (PP2LAL2PPException e) {
                e.printStackTrace();
            }
        }

        return count;
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

            total = Regex.replace("{$" + keyValue[i], total, keyValue[i + 1]);
            total = Regex.replaceFirst("%[0-9]+\\}", total, Util.makeString(" ", tab));
        }

        return total;
    }

    /**
     * Get the amount of characters of space is reserved for the given variable idk.
     */
    private int getSpace(String source, String name) {
        try {
            Pattern regex = Regex.compile("\\{\\$" + name + "\\%(\\d+)?\\}");
            Matcher matcher = regex.matcher(source);

            if (!matcher.find()) {
                throw new CompilerException("Template hasn't been set up correctly ({$" +
                        name + "%#}).");
            }

            String result = matcher.group(1);

            return Integer.parseInt(result);
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
        if (contents != null) {
            return contents;
        }

        File file = new File(Regex.replaceAll("^/", getPath(), ""));
        if (file.exists()) {
            String contents = new FileWorker(file).read();
            return this.contents = contents;
        }

        InputStream is = getClass().getResourceAsStream(getPath());
        StreamWorker sw = new StreamWorker(is);
        String total = sw.read();
        return this.contents = total;
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
