package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.ParseException;
import nl.hannahsten.pp2lal2pp.api.APIFunction;
import nl.hannahsten.pp2lal2pp.compiler.PointerPacker;

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
     * All the global arrays in the program.
     */
    private final List<GlobalArray> globalArrays;

    /**
     * Dictionary where the names of the global variables are mapped to their location in {@link
     * Program#globalVariables}.
     */
    private final Map<String, Integer> globalVariableIndices;

    /**
     * Dictionary where the names of the global arrays are mapped to their location in {@link
     * Program#globalArrays}.
     */
    private final Map<String, Integer> globalArrayIndices;

    /**
     * List of all the definitions (using define).
     */
    private final List<Definition> definitions;

    /**
     * Squishes all global variables and arrays together while skipping banned global base
     * segments.
     *
     * @param maxGlobalBaseSize
     *          The maximum memory size of the global base storage.
     */
    public static void packGlobalVariables(Program program, int maxGlobalBaseSize) {
        List<GlobalVariable> globals = program.getGlobalVariables();
        List<GlobalArray> arrays = program.getGlobalArrays();
        List<PointerPacker.PointerObject> pointerObjects = new ArrayList<>(globals.size() + arrays.size());

        for (GlobalVariable global : globals) {
            pointerObjects.add(new PointerPacker.PointerObject(global, global.getPointer()));
        }
        for (GlobalArray array : arrays) {
            pointerObjects.add(new PointerPacker.PointerObject(array, array.getPointer(), array.size()));
        }

        PointerPacker packer = new PointerPacker(2, pointerObjects, GlobalVariable.getBannedLocations(), maxGlobalBaseSize);
        packer.pack().forEach((obj) -> {
            if (obj.getObject() instanceof GlobalVariable) {
                GlobalVariable global = (GlobalVariable)obj.getObject();
                global.setPointer(obj.getPointer());
            }
            if (obj.getObject() instanceof GlobalArray) {
                GlobalArray array = (GlobalArray)obj.getObject();
                array.setPointers(obj.getPointer());
            }
        });
    }

    public Program() {
        functions = new ArrayList<>();
        functionIndices = new HashMap<>();
        globalVariables = new ArrayList<>();
        globalVariableIndices = new HashMap<>();
        globalArrays = new ArrayList<>();
        globalArrayIndices = new HashMap<>();
        apiFunctions = new HashSet<>();
        definitions = new ArrayList<>();
    }

    /**
     * Squishes all global variables and arrays together while skipping banned global base
     * segments.
     *
     * @param maxGlobalBaseSize
     *          The maximum memory size of the global base storage.
     */
    public void packGlobalVariables(int maxGlobalBaseSize) {
        Program.packGlobalVariables(this, maxGlobalBaseSize);
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
     * @return An unmodifyable list of global arrays.
     */
    public List<GlobalArray> getGlobalArrays() {
        return globalArrays;
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
     * Adds a global array to the program.
     */
    public void addGlobalArray(GlobalArray globalArray) {
        globalArrays.add(globalArray);
        globalArrayIndices.put(globalArray.getName(), globalArrays.size() - 1);
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

    /**
     * Get the global array object with the given name.
     *
     * @param name
     *         The name of the global array to look up.
     * @return The global array with the given name.
     */
    public Optional<GlobalArray> getGlobalArray(String name) {
        if (globalArrayIndices.get(name) == null) {
            return Optional.empty();
        }

        int index = globalArrayIndices.get(name);
        return Optional.of(globalArrays.get(index));
    }

    /**
     * Get the definition with the given name.
     *
     * @param name The name of the definition.
     */
    public Optional<Definition> getDefinition(String name) {
        for (Definition definition : definitions) {
            if (definition.getName().equals(name)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
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
                ", globalArrays=" + globalArrays +
                ", globalArrayIndices=" + globalArrayIndices +
                '}';
    }
}
