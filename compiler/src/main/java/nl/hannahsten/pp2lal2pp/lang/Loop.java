package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

/**
 * @author Hannah Schellekens
 */
public class Loop implements Element, Identifyable {

    /**
     * The unique id of the loop.
     */
    private final int id;

    /**
     * The variable that keeps track of the counting.
     */
    private final Variable base;

    /**
     * The starting value of the base variable.
     */
    private final Value from;

    /**
     * The end value of the base variable.
     */
    private final Value to;

    /**
     * The amount the base value changes per iteration.
     */
    private final Value step;

    /**
     * The block of code to iterate.
     */
    private final Block content;

    public Loop(Block content, Variable base, Value from, Value to, Value step) {
        this.id = IDManager.newId();
        this.content = content;
        this.base = base;
        this.from = from;
        this.to = to;
        this.step = step;
    }

    public Variable getBase() {
        return base;
    }

    public Value getFrom() {
        return from;
    }

    public Value getTo() {
        return to;
    }

    public Value getStep() {
        return step;
    }

    public Block getContent() {
        return content;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return Value.EMPTY;
    }

    @Override
    public String toString() {
        return "loop(" + base + " from " + from + " to " + to + " step " + step + "){" + content
                + "}";
    }
}
