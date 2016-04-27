package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.PP2LAL2PP;

/**
 * Represents an if-else statement.
 *
 * @author Ruben Schellekens
 */
public class IfElse implements Element, Identifyable {

    /**
     * The unique id of the statement.
     */
    private int id;

    /**
     * The expression that determines if the if or else block gets executed.
     *
     * If the expression evaluates to true (or 1), then if gets executed, otherwise the else
     * block must get executed.
     */
    private Element expression;

    /**
     * The block to be executed when the expression holds.
     */
    private Block ifBlock;

    /**
     * The block to be executed when the expression doesn't hold.
     */
    private Block elseBlock;

    public IfElse(Element expression, Block ifBlock) {
        this.id = PP2LAL2PP.globalId++;
        this.expression = expression;
        this.ifBlock = ifBlock;
        this.elseBlock = Block.EMPTY;
    }

    public IfElse(Element expression, Block ifBlock, Block elseBlock) {
        this.id = PP2LAL2PP.globalId++;
        this.expression = expression;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    public Element getExpression() {
        return expression;
    }

    public Block getIfBlock() {
        return ifBlock;
    }

    public Block getElseBlock() {
        return elseBlock;
    }

    @Override
    public Value getValue() {
        return Value.EMPTY;
    }

    @Override
    public int getId() {
        return id;
    }
}
