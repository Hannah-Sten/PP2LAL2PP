package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

/**
 * Represents an if-else statement.
 *
 * @author Hannah Schellekens
 */
public class IfElse implements Element, Identifyable {

    /**
     * The unique id of the statement.
     */
    private final int id;

    /**
     * The expression that determines if the if or else block gets executed.
     *
     * If the expression evaluates to true (or 1), then if gets executed, otherwise the else
     * block must get executed.
     */
    private final Operation expression;

    /**
     * The block to be executed when the expression holds.
     */
    private final Block ifBlock;

    /**
     * The block to be executed when the expression doesn't hold.
     */
    private final Block elseBlock;

    public IfElse(Operation expression, Block ifBlock) {
        this.id = IDManager.newId();
        this.expression = expression;
        this.ifBlock = ifBlock;
        this.elseBlock = Block.EMPTY;
    }

    public IfElse(Operation expression, Block ifBlock, Block elseBlock) {
        this.id = IDManager.newId();
        this.expression = expression;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    public Operation getExpression() {
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

    @Override
    public String toString() {
        return "if (" + expression + "){" + ifBlock + "}\nelse{" + elseBlock + "}";
    }
}
