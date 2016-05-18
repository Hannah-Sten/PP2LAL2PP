package nl.rubensten.pp2lal2pp.api;

import nl.rubensten.pp2lal2pp.util.Template;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Api function templates should have the name "api-[FUNCTIONNAME].template" and function call
 * templates should have the name "api-invoke-[FUNCTIONNAME].template".
 *
 * @author Ruben Schellekens
 */
public class APIFunction {

    private static Set<String> apiFunctions = new HashSet<String>() {{
        add("exit");
        add("getButtonStates");
        add("isButtonPressed");
        add("setLEDs");
        add("setLED");
        add("set7Segment");
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
