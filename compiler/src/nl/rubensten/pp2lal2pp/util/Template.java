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
     * <code>{$VALUE}</code> The numerical value.
     */
    EQU("equ.template");

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
