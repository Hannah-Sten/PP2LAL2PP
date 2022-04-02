package nl.hannahsten.pp2lal2pp.api;

import nl.hannahsten.pp2lal2pp.util.Template;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Api function templates should have the name "api-[FUNCTIONNAME].template" and function call
 * templates should have the name "api-invoke-[FUNCTIONNAME].template".
 *
 * @author Hannah Schellekens
 */
public class APIFunction {

    private static final Set<String> apiFunctions = new HashSet<String>() {{
        add("exit");
        add("getInputStates");
        add("isInputOn");
        add("setOutput");
        add("setSingleOutput");
        add("set7Segment");
        add("getPattern");
        add("getNumPattern");
        add("getAnalogStates");
        add("getAnalog");
        add("getTimer");
        add("addTimer");
        add("setTimer");
    }};

    /**
     * Checks if the given function is an API function or not.
     */
    public static boolean isAPIFunction(String name) {
        return apiFunctions.contains(name);
    }

    /**
     * Get the template of the function call of the function that corresponds to the given API
     * function name.
     *
     * @param name
     *         The name of the function to get the invoke template of.
     * @return The invoke template of the given api function.
     */
    public static Optional<Template> getInvokeTemplate(String name) {
        for (Template temp : Template.values()) {
            if (temp.getFilePath().equals("api-invoke-" + name + ".template")) {
                return Optional.of(temp);
            }
        }

        return Optional.empty();
    }

    /**
     * Get the template of the implementation of the function that corresponds to the given API
     * function name.
     *
     * @param name
     *         The name of the function to get the template of.
     * @return The template of the given api function.
     */
    public static Optional<Template> getImplementationTemplate(String name) {
        for (Template temp : Template.values()) {
            if (temp.getFilePath().equals("api-" + name + ".template")) {
                return Optional.of(temp);
            }
        }

        return Optional.empty();
    }

}
