package nl.rubensten.pp2lal2pp.lang;

import com.sun.javafx.UnmodifiableArrayList;
import nl.rubensten.pp2lal2pp.IDManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class Block implements Iterable, Identifyable {

    /**
     * A block with absolutely NO CONTENTS.
     * <p>
     * The contents can't be modified.
     */
    public static final Block EMPTY = new Block(new UnmodifiableArrayList<>(new Element[0], 0));

    /**
     * The unique id of the code block.
     */
    private int id;

    /**
     * All the statements in chronological order that are present in the code block.
     */
    private List<Element> contents;

    /**
     * Creates an empty block.
     */
    public Block() {
        this.id = IDManager.newId();
        contents = new ArrayList<>();
    }

    public Block(List<Element> contents) {
        this.id = IDManager.newId();
        this.contents = contents;
    }

    /**
     * Copies the elements and put them in a new arraylist.
     */
    public void setContents(List<Element> contents) {
        this.contents = new ArrayList<>(contents);
    }

    public List<Element> getContents() {
        return contents;
    }

    @Override
    public Iterator iterator() {
        return contents.iterator();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Block{" + "contents=" + contents +
                ", id=" + id +
                '}';
    }

}
