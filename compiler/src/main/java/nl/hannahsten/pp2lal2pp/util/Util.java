package nl.hannahsten.pp2lal2pp.util;

/**
 * @author Hannah Schellekens
 */
public class Util {

    /**
     * Repeat elt <code>times</code> times in a new string.
     *
     * @param elt
     *         The element to copy.
     * @param times
     *         the amount of times you want the string to repeat.
     * @return <code>(hai, 5)</code> results in <code>haihaihaihaihai</code>.
     */
    public static String makeString(String elt, int times) {
        StringBuilder sb = new StringBuilder(elt.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(elt);
        }
        return sb.toString();
    }

}
