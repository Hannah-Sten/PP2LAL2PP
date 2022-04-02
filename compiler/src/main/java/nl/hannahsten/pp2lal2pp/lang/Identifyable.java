package nl.hannahsten.pp2lal2pp.lang;

/**
 * Interface for objects that can be identified by a unique number. The number must be unique for
 * every object.
 *
 * @author Hannah Schellekens
 */
public interface Identifyable {

    /**
     * @return The ID of the object.
     */
    int getId();

}
