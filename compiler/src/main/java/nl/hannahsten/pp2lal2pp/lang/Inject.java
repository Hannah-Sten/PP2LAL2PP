package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

/**
 * @author Hannah Schellekens
 */
public class Inject implements Element, Identifyable {

    /**
     * The unique id of the injection.
     */
    private final int id;

    /**
     * The raw Assembly.
     */
    private final String contents;

    public Inject(String contents) {
        this.id = IDManager.newId();
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return new Value(contents);
    }

}
