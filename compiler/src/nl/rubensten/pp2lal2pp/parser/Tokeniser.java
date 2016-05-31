package nl.rubensten.pp2lal2pp.parser;

import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.PP2LAL2PPException;
import nl.rubensten.pp2lal2pp.ParseException;
import nl.rubensten.pp2lal2pp.lang.Operator;
import nl.rubensten.pp2lal2pp.util.Regex;

import java.util.*;

/**
 * @author Ruben Schellekens
 */
public class Tokeniser implements Iterable<String> {

    /**
     * The list of tokens.
     */
    private List<String> tokens;

    /**
     * The original input.
     */
    private String original;

    public Tokeniser(String code) {
        this.original = code;

        String code2 = Regex.replaceAll("((((?<!'),))|(,(?!'))|((?<!'),(?!')))", code, " , ");
        code2 = Regex.replaceAll("((((?<!')\\)))|(\\)(?!'))|((?<!')\\)(?!')))", code2, " ) ");
        code2 = Regex.replaceAll("((((?<!')\\())|(\\((?!'))|((?<!')\\((?!')))", code2, " ( ");
        code2 = Regex.replaceAll("((((?<!')\\-(?![=\\d])))|(\\-(?!['=\\d]))|((?<!')\\-(?!['=\\d])" +
                "))", code2, " - ");
        code2 = Regex.replaceAll("((((?<!')\\}))|(\\}(?!'))|((?<!')\\}(?!')))", code2, " } ");
        code2 = Regex.replaceAll("((((?<!')\\{))|(\\{(?!'))|((?<!')\\{(?!')))", code2, " { ");

        for (Operator op : Operator.values()) {
            String c = op.getRegexSign();
            code2 = Regex.replaceAll("((((?<!')" + c + "))|(" + c + "(?!'))|((?<!')" + c + "(?!')" +
                    "))", code2, "櫓\uF214(" + op.name() + ")");
        }

        for (Operator op : Operator.values()) {
            code2 = Regex.replace("櫓\uF214(" + op.name() + ")", code2, " " + op.getSign() + " ");
        }

        String[] strings = Regex.split("\\s+", code2);

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
    public String getToken(int index) throws IndexOutOfBoundsException {
        return tokens.get(index);
    }

    /**
     * @return The amount of lines in the tokeniser.
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Gets the last token.
     *
     * @throws ParseException when there are no tokens.
     */
    public String last() throws ParseException {
        if (sizeNoComments() == 0) {
            throw new ParseException("There is no last token.");
        }

        return getToken(sizeNoComments() - 1);
    }

    /**
     * Checks if the token at the given index equals the given value.
     *
     * @param index
     *         The index of the token starting with 0.
     * @param value
     *         The value of the token to check for.
     * @return <code>true</code> if the token at index <code>index</code> has the given value,
     * <code>false /code> otherwise.
     */
    public boolean equals(int index, String value) {
        if (index >= size()) {
            return false;
        }

        return tokens.get(index).equals(value);
    }

    /**
     * Joins all the tokens from index <code>start</code> to and including <code>start +
     * length</code> with a given delimiter.
     *
     * @param start
     *         The index of the first token starting with 0.
     * @param length
     *         The amount of tokens to join.
     * @param delimiter
     *         The sequence to put between each token.
     * @return A string where all tokens in the given interval are seperated by a given string.
     * @throws PP2LAL2PPException
     *         when <code>start + length</code> exceeds the amount of tokens minus 1.
     */
    public String join(int start, int length, String delimiter) throws PP2LAL2PPException {
        if (start + length - 1 >= size()) {
            throw new PP2LAL2PPException("Index out of bounds (" + (start + length) + "). Expected " +
                    "an index < " + size());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            sb.append(tokens.get(i));

            if (i != start + length - 1) {
                sb.append(delimiter);
            }
        }

        return sb.toString();
    }

    /**
     * Checks if there is a first token, if there is then it checks if the first token equals the
     * given string.
     *
     * @return <code>true</code> if there is a first token AND if the first token equals the given
     * string, <code>false</code> otherwise.
     */
    public boolean isFirst(String string) {
        return equals(0, string);
    }

    /**
     * Checks if the first token that is not <code>ignore</code> equals the given
     * <code>string</code>.
     *
     * @return <code>true</code> if the first token not being <code>ignore</code> equals
     * <code>string</code>, <code>false</code> otherwise.
     */
    public boolean isFirstIgnore(String string, String ignore) {
        for (String token : tokens) {
            if (token.equals(ignore)) {
                continue;
            }

            return token.equals(string);
        }

        return false;
    }

    /**
     * Checks if the first two tokens that are not <code>ignore</code> equal the given
     * <code>first</code> and <code>second</code> strings.
     *
     * @return <code>true</code> if the first twi tokens not being <code>ignore</code> equal
     * <code>first</code> and <code>second</code>, <code>false</code> otherwise.
     */
    public boolean isFirstTwoIgnore(String first, String second, String ignore) {
        boolean firstDone = false;

        for (String token : tokens) {
            if (token.equals(ignore)) {
                continue;
            }

            if (token.equals(first) && !firstDone) {
                firstDone = true;
                continue;
            }

            return firstDone && token.equals(second);
        }

        return false;
    }

    /**
     * Checks if the last token that is not <code>ignore</code> equals the given
     * <code>string</code>.
     *
     * @return <code>true</code> if the last token not being <code>ignore</code> equals
     * <code>string</code>, <code>false</code> otherwise.
     */
    public boolean isLastIgnore(String string, String ignore) {
        for (int i = size() - 1; i >= 0; i--) {
            String token = tokens.get(i);
            if (token.equals(ignore)) {
                continue;
            }

            return token.equals(string);
        }

        return false;
    }

    /**
     * Counts all the tokens that aren't comments.
     */
    public int sizeNoComments() {
        int count = 0;
        for (String string : this) {
            if (string.startsWith(Constants.LINE_COMMENT_START)) {
                return count;
            }

            count++;
        }
        return count;
    }

    /**
     * @return The original input for the tokeniser.
     */
    public String getOriginal() {
        return original;
    }

    @Override
    public Iterator<String> iterator() {
        return tokens.listIterator();
    }

}
