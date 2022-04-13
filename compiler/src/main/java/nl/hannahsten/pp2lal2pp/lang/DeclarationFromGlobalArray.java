package nl.hannahsten.pp2lal2pp.lang;

/**
 * Variable declaration where the value is obtained from a global array.
 *
 * @author Hannah Schellekens
 */
public class DeclarationFromGlobalArray extends Declaration {

    private final GlobalArrayRead arrayRead;

    public DeclarationFromGlobalArray(Variable variable, GlobalArrayRead arrayRead) {
        super(variable, DeclarationScope.LOCAL);
        this.arrayRead = arrayRead;
    }

    public GlobalArrayRead getArrayRead() {
        return arrayRead;
    }

    @Override
    public Value getDeclaration() {
        return null;
    }

    @Override
    public String toString() {
        return "DeclarationFromGlobalArray{" + variable + ", arrayRead=" + arrayRead + '}';
    }
}
