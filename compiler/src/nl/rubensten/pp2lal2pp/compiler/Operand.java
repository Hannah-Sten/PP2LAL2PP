package nl.rubensten.pp2lal2pp.compiler;

/**
 * Just represents a 'thing' in a 'operation'. Could have called it OperandElement, but that sounds
 * not nice enough.
 *
 * @author Ruben Schellekens
 */
public interface Operand {

    /**
     * Fetches the operand that is needed for the <code>PUSH</code> instruction.
     * <p>
     * E.g. a number 4 will return <code>4</code>, a variable will return <code>[SP+??]</code>.
     */
    String getValueLocation();

}
