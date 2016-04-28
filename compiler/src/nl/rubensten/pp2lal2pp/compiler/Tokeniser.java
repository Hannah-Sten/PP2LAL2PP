package nl.rubensten.pp2lal2pp.compiler;

import com.sun.javafx.UnmodifiableArrayList;
import nl.rubensten.pp2lal2pp.lang.Operator;

import java.util.*;

/**
 * @author Ruben Schellekens
 */
public class Tokeniser implements Iterable<String> {

    /**
     * The list of tokens.
     */
    private List<String> tokens;

    public Tokeniser(String code) {
        String code2 = code.replace("{", " { ")
                .replace("}", " } ")
                .replace("-", " - ")
                .replace("!", " !")
                .replace("(", " ( ")
                .replace(")", " ) ");

        for (Operator op : Operator.values()) {
            code2 = code2.replace(op.getSign(), "櫓\uF214(" + op.name() + ")");
        }

        for (Operator op : Operator.values()) {
            code2 = code2.replace("櫓\uF214(" + op.name() + ")", " " + op.getSign() + " ");
        }

        String[] strings = code2.split("\\s+");

        tokens = new ArrayList<>();
        for (String string : strings) {
            if (!string.isEmpty()) {
                tokens.add(string);
            }
        }
        tokens = Collections.unmodifiableList(tokens);
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
