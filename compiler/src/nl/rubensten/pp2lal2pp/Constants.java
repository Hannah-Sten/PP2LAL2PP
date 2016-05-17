package nl.rubensten.pp2lal2pp;

/**
 * @author Ruben Schellekens
 */
public class Constants {

    /**
     * The register that is used to point to the start of the global variable storage.
     */
    public static final String REG_GLOBAL_BASE = "GB";

    /**
     * The register that is used to hold the stack pointer.
     */
    public static final String REG_STACK_POINTER = "SP";

    /**
     * The register that stores where the location of the IOAREA is.
     */
    public static final String REG_IOAREA = "R5";

    /**
     * The register that is used for return values.
     */
    public static final String REG_RETURN = "R4";

    /**
     * The register that is used for storing variable value fetches.
     */
    public static final String REG_VARIABLE = "R3";

    /**
     * The string that denotes the start of a line comment.
     */
    public static final String LINE_COMMENT_START = "#";

    /**
     * The string that denotes the start of a multi-line comment.
     */
    public static final String MULTI_COMMENT_START = ";#";

    /**
     * The string that denotes the end of a multi-line-comment.
     */
    public static final String MULTI_COMMENT_END = ";##";

}
