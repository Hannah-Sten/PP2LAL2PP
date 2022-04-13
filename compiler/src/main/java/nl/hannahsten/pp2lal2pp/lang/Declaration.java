package nl.hannahsten.pp2lal2pp.lang;

/**
 * Represents a variable declaration.
 *
 * @author Hannah Schellekens
 */
public class Declaration implements Element {

    /**
     * The variable.
     */
    protected final Variable variable;

    /**
     * The initial value of the variable.
     */
    protected final Value declaration;

    /**
     * The scope of the variable.
     */
    protected final DeclarationScope scope;

    public Declaration(Variable variable, DeclarationScope scope) {
        this.variable = variable;
        this.declaration = Number.ZERO;
        this.scope = scope;
    }

    public Declaration(Variable variable, Value value, DeclarationScope scope) {
        this.variable = variable;
        this.declaration = value;
        this.scope = scope;
    }

    /**
     * Create a new local declaration.
     */
    public static Declaration declareLocal(Variable variable) {
        return new Declaration(variable, DeclarationScope.LOCAL);
    }

    /**
     * Create a new local declaration with a default value.
     */
    public static Declaration declareLocal(Variable variable, Value value) {
        return new Declaration(variable, value, DeclarationScope.LOCAL);
    }

    /**
     * Create a new global declaration.
     */
    public static Declaration declareGlobal(Variable variable) {
        return new Declaration(variable, DeclarationScope.GLOBAL);
    }

    /**
     * Create a new global declaration with a default value.
     */
    public static Declaration declareGlobal(Variable variable, Value value) {
        return new Declaration(variable, value, DeclarationScope.GLOBAL);
    }

    public DeclarationScope getScope() {
        return scope;
    }

    public Variable getVariable() {
        return variable;
    }

    public Value getDeclaration() {
        return declaration;
    }

    @Override
    public Value getValue() {
        return Value.EMPTY;
    }

    @Override
    public String toString() {
        return "(" + variable + ":=" + getDeclaration().stringRepresentation() + ")";
    }

    public enum DeclarationScope {
        LOCAL, GLOBAL;
    }
}
