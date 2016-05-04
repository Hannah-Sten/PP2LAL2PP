package nl.rubensten.pp2lal2pp.lang;

/**
 * @author Ruben Schellekens
 */
public class Comment implements Element {

    /**
     * The comment.
     */
    private String contents;

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
