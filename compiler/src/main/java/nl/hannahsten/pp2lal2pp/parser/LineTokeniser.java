package nl.hannahsten.pp2lal2pp.parser;

import com.sun.javafx.UnmodifiableArrayList;
import nl.hannahsten.pp2lal2pp.util.Regex;

import java.util.Iterator;
import java.util.List;

/**
 * @author Hannah Schellekens
 */
public class LineTokeniser implements Iterable<String> {

    /**
     * A list of all lines in the given code.
     */
    private List<String> lines;

    public LineTokeniser(String code) {
        String[] strings = Regex.split("(\\s)*\\n(\\s)*", code);
        lines = new UnmodifiableArrayList<>(strings, strings.length);
    }

    /**
     * Gets the line with line number <code>index</code> starting with 0.
     */
    public String getLine(int index) {
        return lines.get(index);
    }

    /**
     * @return The amount of lines in the tokeniser.
     */
    public int size() {
        return lines.size();
    }

    @Override
    public Iterator<String> iterator() {
        return lines.listIterator();
    }

}
