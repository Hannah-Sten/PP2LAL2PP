package nl.rubensten.pp2lal2pp.lang;

import com.sun.javafx.UnmodifiableArrayList;
import com.sun.javafx.collections.UnmodifiableListSet;
import nl.rubensten.pp2lal2pp.api.APIFunction;

import java.util.*;

/**
 * @author Ruben Schellekens
 */
public class Program {

    /**
     * All the functions that are in the program.
     */
    private List<Function> functions;

    /**
     * A set of all types of API-functions that are included in the program.
     * <p>
     * Based on this information, certain assembly parts have to be included in the source code.
     */
    private Set<Class> apiFunctionTypes;

    /**
     * A set of all api functions that are included in the program.
     */
    private Set<APIFunction> apiFunctions;

    /**
     * Dictionary where the names of the functions are mapped to their location in {@link
     * Program#functions}.
     */
    private Map<String, Integer> functionIndices;

    /**
     * All the global variables in the program.
     */
    private List<GlobalVariable> globalVariables;

    /**
     * Dictionary where the names of the global variables are mapped to their location in {@link
     * Program#globalVariables}.
     */
    private Map<String, Integer> globalVariableIndices;

    public Program() {
        functions = new ArrayList<>();
        functionIndices = new HashMap<>();
        globalVariables = new ArrayList<>();
        globalVariableIndices = new HashMap<>();
        apiFunctionTypes = new HashSet<>();
        apiFunctions = new HashSet<>();
    }

    /**
     * @return An <b>unmodifyable</b> set containing all API functions that have been called.
     */
    public Set<APIFunction> getApiFunctions() {
        return Collections.unmodifiableSet(apiFunctions);
    }

    /**
     * Registers that the given APIFunction is being used by the program.
     */
    public void addAPIFunction(APIFunction apiFunction) {
        boolean newElt = apiFunctionTypes.add(apiFunction.getClass());

        if (newElt) {
            apiFunctions.add(apiFunction);
        }
    }

    /**
     * @return An unmodifyable list of the functions.
     */
    public List<Function> getFunctions() {
        return Collections.unmodifiableList(functions);
    }

    /**
     * @return An unmodifyable list of the global variables.
     */
    public List<GlobalVariable> getGlobalVariables() {
        return Collections.unmodifiableList(globalVariables);
    }

    /**
     * Adds a function to the program.
     */
    public void addFunction(Function function) {
        functions.add(function);
        functionIndices.put(function.getName(), functions.size());
    }

    /**
     * Get the function object with the given name.
     *
     * @param name
     *         The name of the function to look up.
     * @return The function with the given name.
     */
    public Function getFunction(String name) {
        int index = functionIndices.get(name);
        return functions.get(index);
    }

    /**
     * Adds a global variable to the program.
     */
    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariables.add(globalVariable);
        globalVariableIndices.put(globalVariable.getName(), globalVariables.size());
    }

    /**
     * Get the global variable object with the given name.
     *
     * @param name
     *         The name of the global variable to look up.
     * @return The global variable with the given name.
     */
    public GlobalVariable getGlobalVariable(String name) {
        int index = globalVariableIndices.get(name);
        return globalVariables.get(index);
    }

}
