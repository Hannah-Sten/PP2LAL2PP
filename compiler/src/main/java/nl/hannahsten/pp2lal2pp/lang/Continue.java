package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

/**
 * @author Hannah Schellekens
 */
public class Continue implements Identifyable, Element {

    /**
     * The unique ID of the return statement.
     */
    private final int id;

    public Continue() {
        this.id = IDManager.newId();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return Value.EMPTY;
    }

}
