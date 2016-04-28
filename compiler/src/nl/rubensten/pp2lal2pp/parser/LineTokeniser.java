package nl.rubensten.pp2lal2pp.parser;

import com.sun.javafx.UnmodifiableArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LineTokeniser implements Iterable<String> {

    /**
     * A list of all lines in the given code.
     */
    private List<String> lines;

    public LineTokeniser(String code) {
        String[] strings = code.split("(\\s)*\\n(\\s)*");
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
        return lines.iterator();
    }

}
