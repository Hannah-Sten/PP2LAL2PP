package nl.hannahsten.pp2lal2pp.lang;

/**
 * Read an element from an array at a specific index.
 *
 * @author Hannah Schellekens
 */
public class GlobalArrayRead implements Element {

    /**
     * The array to read from.
     */
    private final GlobalArray array;

    /**
     * Representation of the index.
     */
    private final ArrayAccess access;

    public GlobalArrayRead(GlobalArray array, ArrayAccess access) {
        this.array = array;
        this.access = access;
    }

    public GlobalArray getArray() {
        return array;
    }

    public ArrayAccess getAccess() {
        return access;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "GlobalArrayRead[" + access.toString() + "]{array=" + array +
                ", access=" + access +
                '}';
    }
}
