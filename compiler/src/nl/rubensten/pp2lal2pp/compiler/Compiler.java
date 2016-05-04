package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.lang.Program;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Ruben Schellekens
 */
public class Compiler {

    /**
     * The file to output the result to.
     */
    private File output;

    /**
     * The parsed program that should be compiled.
     */
    private Program input;

    public Compiler(File output, Program input) {
        this.output = output;
        this.input = input;
    }

    /**
     * Compiles the input and writes it to the given output file.
     */
    public void compile() throws CompilerException {
        try {
            FileWriter fw = new FileWriter(output);
            fw.write("\nTEST\n");
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
