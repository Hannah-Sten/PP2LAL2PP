package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.IDManager;

/**
 * @author Ruben Schellekens
 */
public class Inject implements Element, Identifyable {

    /**
     * The unique id of the injection.
     */
    private int id;

    /**
     * The raw Assembly.
     */
    private String contents;

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
