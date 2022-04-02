package nl.hannahsten.pp2lal2pp.lang;

/**
 * @author Hannah Schellekens
 */
@FunctionalInterface
public interface OperationFunction<T> {

    T operate(T first, T second);

}
