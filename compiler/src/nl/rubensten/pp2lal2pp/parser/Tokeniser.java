package nl.rubensten.pp2lal2pp.parser;

import com.sun.javafx.UnmodifiableArrayList;

import java.util.Iterator;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class Tokeniser implements Iterable<String> {

    /**
     * The list of tokens.
     */
    private List<String> tokens;

    public Tokeniser(String code) {
        String[] strings = code.split("\\s+");
        tokens = new UnmodifiableArrayList<>(strings, strings.length);
    }

    /**
     * Gets the line with line number <code>index</code> starting with 0.
     */
    public String getLine(int index) {
        return tokens.get(index);
    }

    /**
     * @return The amount of lines in the tokeniser.
     */
    public int size() {
        return tokens.size();
    }

    @Override
    public Iterator<String> iterator() {
        return tokens.iterator();
    }

}
