package nl.hannahsten.pp2lal2pp;

/**
 * @author Hannah Schellekens
 */
public class IDManager {

    /**
     * Tracks the latest global ID, must be increased on each assignment.
     */
    private static int globalId = 0;

    /**
     * @return A new unique ID number.
     */
    public static int newId() {
        return globalId++;
    }

}
