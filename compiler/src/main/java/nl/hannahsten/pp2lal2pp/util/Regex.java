package nl.hannahsten.pp2lal2pp.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Makes sure regex is only compiled once.
 *
 * @author Hannah Schellekens
 */
public enum Regex {

    INSTANCE;

    /**
     * Hashmap with all compiled patterns.
     */
    private Map<String, Pattern> patterns = new HashMap<>();

    /**
     * Compiles said regex if it hasn't been compiled yet, otherwise use the already compiled
     * variant.
     */
    public static Pattern compile(String regex) {
        Pattern pattern = INSTANCE.patterns.get(regex);

        if (pattern != null) {
            return pattern;
        }

        pattern = Pattern.compile(regex);
        INSTANCE.patterns.put(regex, pattern);
        return pattern;
    }

    /**
     * Compiles a string literal as regex (with compile mode {@link Pattern#LITERAL}). If it has
     * already been compiled once, it will use the already compiled version.
     */
    public static Pattern compileLiteral(String string) {
        Pattern pattern = INSTANCE.patterns.get(string);

        if (pattern != null) {
            return pattern;
        }

        pattern = Pattern.compile(string, Pattern.LITERAL);
        INSTANCE.patterns.put(string, pattern);
        return pattern;
    }

    /**
     * Checks if the given source string maches the regular expression.
     */
    public static boolean matches(String regex, String source) {
        return compile(regex).matcher(source).matches();
    }

    /**
     * Splits the given source with the given regex
     */
    public static String[] split(String regex, String source) {
        return compile(regex).split(source);
    }

    /**
     * Replaces all occurences of toFind in the source by replacement.
     */
    public static String replace(String toFind, String source, String replacement) {
        return compileLiteral(toFind).matcher(source).replaceAll(
                Matcher.quoteReplacement(replacement));
    }

    /**
     * Replaces all occurences of the regex in the source by a certain replacement.
     */
    public static String replaceAll(String regex, String source, String replacement) {
        return compile(regex).matcher(source).replaceAll(replacement);
    }

    /**
     * Replaces the first occurence of the regex in the source by a certain replacement.
     */
    public static String replaceFirst(String regex, String source, String replacement) {
        return compile(regex).matcher(source).replaceFirst(replacement);
    }

}
