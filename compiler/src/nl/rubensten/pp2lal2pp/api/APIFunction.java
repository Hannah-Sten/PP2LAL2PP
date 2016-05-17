package nl.rubensten.pp2lal2pp.api;

/**
 * @author Ruben Schellekens
 */
public interface APIFunction {

    /**
     * The name of the function.
     */
    String getName();

    /**
     * The assembly implementation of the function.
     */
    String getDeclaration();

}
