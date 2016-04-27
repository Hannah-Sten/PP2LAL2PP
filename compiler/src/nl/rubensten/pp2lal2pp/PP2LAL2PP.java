package nl.rubensten.pp2lal2pp;

import nl.rubensten.pp2lal2pp.lang.GlobalVariable;
import nl.rubensten.pp2lal2pp.lang.Variable;
import nl.rubensten.pp2lal2pp.parser.FileParser;

import java.io.File;

/**
 * Practicum Processor To Learn Assembly Language To Preserve Prosperity-Language compiler.
 *
 * @author Ruben Schellekens, Sten Wessel
 * @version 0.1
 */
public class PP2LAL2PP {

    /**
     * Tracks the latest global ID, must be increased on each assignment.
     */
    public static int globalId = 0;

    public static void main(String[] args) {
        File file = new File("test.pp2");
        FileParser parser = new FileParser(file);
    }

}
