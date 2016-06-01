package nl.rubensten.pp2lal2pp.lang;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ruben Schellekens
 */
public class GlobalVariable extends Variable {

    /**
     * Counter that tracks the location of the previous global variable.
     */
    private static int pointerCounter = 2;

    /**
     * A list containing all the global base addresses that can't be used to store values.
     */
    private static Set<Integer> bannedLocations = new HashSet<>();

    /**
     * Prevents the given pointer from being used.
     *
     * @param pointer
     *         The global base address  to ban from being used.
     */
    public static void banPointer(int pointer) {
        bannedLocations.add(pointer);
    }

    /**
     * Checks if the given pointer is banned from being used by global base or not.
     *
     * @param pointer
     *         The address to check for.
     * @return <code>true</code> if the pointer is banned, <code>false</code> if it is available.
     */
    public static boolean isBanned(int pointer) {
        return bannedLocations.contains(pointer);
    }

    /**
     * Updates the pointer counter to make sure the counter doesn't contain a banned number.
     */
    public static void adjustCounter() {
        while (bannedLocations.contains(pointerCounter)) {
            pointerCounter++;
        }
    }

    /**
     * The comment that is placed directly above the global variable.
     */
    private Comment comment;

    public GlobalVariable(String name) {
        super(name);
        adjustCounter();
        setPointer(pointerCounter++);
    }

    public GlobalVariable(String name, Value value) {
        super(name, value);
        adjustCounter();
        setPointer(pointerCounter++);
    }

    public GlobalVariable(String name, Value value, Comment comment) {
        this(name, value);
        this.comment = comment;
    }

    public GlobalVariable(String name, Comment comment) {
        this(name);
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

}
