package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.PP2LAL2PPException;

/**
 * @author Ruben Schellekens
 */
public class ElementReturn extends Return {

    /**
     * The element to return.
     */
    private Element value;

    public ElementReturn(Element value) {
        this.value = value;
    }

    @Override
    public Value getReturnValue() {
        throw new PP2LAL2PPException("you cannot get a value from an element return. use " +
                "getElement instead");
    }

    /**
     * The amazing element wojooow.
     */
    public Element getElement() {
        return value;
    }

}
