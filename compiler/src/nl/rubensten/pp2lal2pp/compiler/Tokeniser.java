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
                .replace("!", " ! ")
                .replace("(", " ( ")
                .replace(")", " ) ")
                .replace(",", " , ");

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
     * Gets the token with token number <code>index</code> starting with 0.
     */
    public String getToken(int index) {
        return tokens.get(index);
    }

    /**
     * @return The amount of lines in the tokeniser.
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Checks if there is a first token, if there is then it checks if the first token equals the
     * given string.
     *
     * @return <code>true</code> if there is a first token AND if the first token equals the
     * given string, <code>false</code> otherwise.
     */
    public boolean isFirst(String string) {
        if (size() == 0) {
            return false;
        }

        return tokens.get(0).equals(string);
    }

    @Override
    public Iterator<String> iterator() {
        return tokens.iterator();
    }

}
