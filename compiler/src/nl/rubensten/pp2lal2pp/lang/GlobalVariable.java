package nl.rubensten.pp2lal2pp.lang;

/**
 * @author Ruben Schellekens
 */
public class GlobalVariable extends Variable {

    /**
     * Counter that tracks the location of the previous global variable.
     */
    public static int pointerCounter = 0;

    public GlobalVariable(String name) {
        super(name);
        setPointer(++pointerCounter);
    }

}
