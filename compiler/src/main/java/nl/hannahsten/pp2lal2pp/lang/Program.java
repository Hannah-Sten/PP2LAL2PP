package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.ParseException;
import nl.hannahsten.pp2lal2pp.api.APIFunction;

import java.util.*;

/**
 * @author Hannah Schellekens
 */
public class Program {

    /**
     * The lines of the header comment.
     */
    private List<String> header;

    /**
     * All the functions that are in the program.
     */
    private final List<Function> functions;

    /**
     * A set of all names of api functions that are included in the program.
     */
    private Set<String> apiFunctions;

    /**
     * Dictionary where the names of the functions are mapped to their location in {@link
     * Program#functions}.
     */
    private final Map<String, Integer> functionIndices;

    /**
     * All the global variables in the program.
     */
    private final List<GlobalVariable> globalVariables;

    /**
     * Dictionary where the names of the global variables are mapped to their location in {@link
     * Program#globalVariables}.
     */
    private final Map<String, Integer> globalVariableIndices;

    /**
     * List of all the definitions (using define).
     */
    private final List<Definition> definitions;

    public Program() {
        functions = new ArrayList<>();
        functionIndices = new HashMap<>();
        globalVariables = new ArrayList<>();
        globalVariableIndices = new HashMap<>();
        apiFunctions = new HashSet<>();
        apiFunctions = new HashSet<>();
        definitions = new ArrayList<>();
    }

    /**
     * @return An <b>unmodifyable</b> set containing all API functions that have been called.
     */
    public Set<String> getApiFunctions() {
        return Collections.unmodifiableSet(apiFunctions);
    }

    /**
     * Checks if the given function is an API function. If so, it adds it to the set of used
     * functions.
     *
     * @param functionName
     *         The name of the function.
     */
    public void registerAPIFunction(String functionName) {
        if (APIFunction.isAPIFunction(functionName)) {
            apiFunctions.add(functionName);
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
     * @return An unmodifyable list of the definitions.
     */
    public List<Definition> getDefinitions() {
        return Collections.unmodifiableList(definitions);
    }

    /**
     * Adds a function to the program.
     */
    public void addFunction(Function function) {
        functionIndices.put(function.getName(), functions.size());
        functions.add(function);
    }

    /**
     * Add a define-statement to the program.
     */
    public void addDefinition(Definition definition) {
        definitions.add(definition);
    }

    /**
     * Get the function object with the given name.
     *
     * @param name
     *         The name of the function to look up.
     * @return The function with the given name.
     */
    public Optional<Function> getFunction(String name) {
        if (!functionIndices.containsKey(name)) {
            return Optional.empty();
        }

        int index = functionIndices.get(name);
        return Optional.of(functions.get(index));
    }

    /**
     * @return Looks up the main function.
     */
    public Function getMainFunction() throws ParseException {
        Optional<Function> main = getFunction("main");
        if (!main.isPresent()) {
            throw new ParseException("Program must contain a main method!");
        }

        return main.get();
    }

    /**
     * Adds a global variable to the program.
     */
    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariables.add(globalVariable);
        globalVariableIndices.put(globalVariable.getName(), globalVariables.size() - 1);
    }

    /**
     * Get the global variable object with the given name.
     *
     * @param name
     *         The name of the global variable to look up.
     * @return The global variable with the given name.
     */
    public Optional<GlobalVariable> getGlobalVariable(String name) {
        if (globalVariableIndices.get(name) == null) {
            return Optional.empty();
        }

        int index = globalVariableIndices.get(name);
        return Optional.of(globalVariables.get(index));
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public List<String> getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "Program{" + "apiFunctions=" + apiFunctions +
                ", functions=" + functions +
                ", apiFunctions=" + apiFunctions +
                ", functionIndices=" + functionIndices +
                ", globalVariables=" + globalVariables +
                ", globalVariableIndices=" + globalVariableIndices +
                '}';
    }
}
