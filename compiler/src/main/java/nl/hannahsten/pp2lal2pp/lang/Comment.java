package nl.hannahsten.pp2lal2pp.lang;

/**
 * @author Hannah Schellekens
 */
public class Comment implements Element {

    /**
     * The comment.
     */
    private final String contents;

    public Comment(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public Value getValue() {
        return new Value(contents);
    }

    @Override
    public String toString() {
        return "/*" + contents + "*/";
    }
}
