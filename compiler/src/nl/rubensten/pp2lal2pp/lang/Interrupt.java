package nl.rubensten.pp2lal2pp.lang;

import java.util.List;

/**
 * @author Sten Wessel
 */
public class Interrupt extends Function {

    public Interrupt(String name, List<String> pp2doc) {
        super(name, pp2doc);
    }

    @Override
    public String toString() {
        return "Interrupt{" + "id=" + id +
                ", name='" + name + '\'' +
                ", contents=" + contents +
                ", variables=" + variables +
                ", pp2doc=" + pp2doc +
                '}';
    }
}
