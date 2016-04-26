package nl.rubensten.pp2lal2pp.lang;

/**
 * @author Ruben Schellekens
 */
public interface Variable extends Identifyable {

    /**
     * @return The memory address where the value of the variable is stored.
     */
    int getPointer();

    /**
     * @return The name of the variable.
     */
    String getName();

}
