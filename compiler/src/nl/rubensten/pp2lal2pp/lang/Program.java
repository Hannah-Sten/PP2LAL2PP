package nl.rubensten.pp2lal2pp.lang;

import com.sun.javafx.UnmodifiableArrayList;
import jdk.nashorn.internal.objects.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruben Schellekens
 */
public class Program {

    /**
     * All the functions that are in the program.
     */
    private List<Function> functions;

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
     * Dictionary where the names of the global variables are mapped to their location in
     * {@link Program#globalVariables}.
     */
    private Map<String, Integer> globalVariableIndices;

    public Program() {
        functions = new ArrayList<>();
        functionIndices = new HashMap<>();
        globalVariables = new ArrayList<>();
        globalVariableIndices = new HashMap<>();
    }

    /**
     * @return An unmodifyable list of the functions.
     */
    public List<Function> getFunctions() {
        return new UnmodifiableArrayList<>((Function[])functions.toArray(), functions.size());
    }

    /**
     * @return An unmodifyable list of the global variables.
     */
    public List<GlobalVariable> getGlobalVariables() {
        return new UnmodifiableArrayList<>((GlobalVariable[])globalVariables.toArray(),
                globalVariables.size());
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
