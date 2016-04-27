package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.PP2LAL2PP;

/**
 * @author Ruben Schellekens
 */
public class Loop implements Element, Identifyable {

    /**
     * The unique id of the loop.
     */
    private int id;

    /**
     * The variable that keeps track of the counting.
     */
    private Variable base;

    /**
     * The starting value of the base variable.
     */
    private Value from;

    /**
     * The end value of the base variable.
     */
    private Value to;

    /**
     * The amount the base value changes per iteration.
     */
    private Value step;

    /**
     * The block of code to iterate.
     */
    private Block content;

    public Loop(Block content, Variable base, Value from, Value to, Value step) {
        this.id = PP2LAL2PP.globalId++;
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

}
