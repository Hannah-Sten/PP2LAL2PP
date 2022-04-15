package nl.hannahsten.pp2lal2pp.compiler;

import nl.hannahsten.pp2lal2pp.PP2LAL2PPException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Squishes all objects in memory in a non optimal way while ignoring banned addresses.
 * Run with pack().
 *
 * @author Hannah Schellekens
 */
public class PointerPacker {

    /**
     * The first pointer address that can be used.
     */
    private final int firstAvailableAddress;

    /**
     * All objects whose pointers need to be calculated.
     */
    private final List<PointerObject> objects;

    /**
     * The addresses that cannot be used.
     */
    private final Set<Integer> bannedAddresses;

    /**
     * The maximum global base memory address to check for.
     */
    private final int maxMemoryAddress;

    public PointerPacker(int firstAvailableAddress, List<PointerObject> objects,
                         Set<Integer> bannedAddresses, int maxMemoryAddress) {
        this.firstAvailableAddress = firstAvailableAddress;
        this.objects = objects;
        this.bannedAddresses = new HashSet<>(bannedAddresses);
        this.maxMemoryAddress = maxMemoryAddress;
    }

    public List<PointerObject> pack() {
        final Set<Integer> unavailableAddresses = new HashSet<>(bannedAddresses);
        objects.sort(PointerObject::compareTo);

        int storedMemory = 0;

        // Sorry for the labels. It sucks, but works.
        coverObjects:
        for (PointerObject toPack : objects) {
            int objectSize = toPack.getSize();
            int checkAddress = firstAvailableAddress;

            // Continue until an available first address is found to fit the whole object.
            firstAddress:
            while (checkAddress <= maxMemoryAddress) {

                // Check if the next few addresses are available.
                for (int offset = 0; offset < objectSize; offset++) {
                    if (unavailableAddresses.contains(checkAddress + offset)) {
                        // If it doesnt fit in this chunk, skip ahead to after the unavailable
                        // memory.
                        checkAddress += (offset + 1);
                        continue firstAddress;
                    }
                }

                // There was no break, the addresses are available.
                for (int offset = 0; offset < objectSize; offset++) {
                    unavailableAddresses.add(checkAddress + offset);
                }
                toPack.pointer = checkAddress;
                storedMemory += toPack.size;

                if (storedMemory > maxMemoryAddress) {
                    throw new PP2LAL2PPException("Maximum global base size exceeded (" + maxMemoryAddress + "), increase with the '-g <size>' argument");
                }

                continue coverObjects;
            }

            throw new PP2LAL2PPException("Maximum global base size exceeded (" + maxMemoryAddress + "), increase with the '-g <size>' argument");
        }

        return objects;
    }

    public List<PointerObject> getObjects() {
        return objects;
    }

    /**
     * @author  Hannah Schellekens
     */
    public static class PointerObject implements Comparable<PointerObject> {

        protected Object object;
        protected int pointer;

        /**
         * The amount of addresses required for this object.
         */
        protected int size;

        /**
         * Creates a pointer object with size 1.
         */
        public PointerObject(Object object, int initialPointer) {
            this(object, initialPointer, 1);
        }

        public PointerObject(Object object, int initialPointer, int size) {
            this.object = object;
            this.pointer = initialPointer;
            this.size = size;
        }

        public Object getObject() {
            return object;
        }

        public int getPointer() {
            return pointer;
        }

        public int getSize() {
            return size;
        }

        @Override
        public int compareTo(PointerObject o) {
            return -1 * Integer.compare(size, o.size);
        }
    }
}
