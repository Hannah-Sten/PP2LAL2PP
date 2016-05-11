package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.lang.GlobalVariable;
import nl.rubensten.pp2lal2pp.lang.Program;
import nl.rubensten.pp2lal2pp.util.FileWorker;
import nl.rubensten.pp2lal2pp.util.Template;
import nl.rubensten.pp2lal2pp.util.Util;

import java.io.File;

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

    /**
     * The compiled output assembly code.
     */
    private StringBuilder assembly;

    public Compiler(File output, Program input) {
        this.output = output;
        this.input = input;
        this.assembly = new StringBuilder();
    }

    /**
     * Compiles the input and writes it to the given output file.
     */
    public void compile() throws CompilerException {
        // Standard header
        assembly.append(Template.BEGIN_CODE);
        assembly.append("\n\n");
        assembly.append(Template.DEFAULT_EQU);
        assembly.append("\n\n");
        compileGlobal();
        assembly.append("\n");
        assembly.append(Template.BEGIN_MAIN);
        assembly.append("\n\n");

        // Main function


        // Other functions


        // @END
        assembly.append(Template.END);

        // Write to file
        write();
    }

    /**
     * Writes all the global variables of the program to the assembly-StringBuilder.
     */
    private void compileGlobal() {
        String equ = Template.EQU.load();

        int space = -1;
        try {
            space = Integer.parseInt(equ.replaceAll(".*\\{\\$NAME%", "").replaceAll("\\}.*", ""));
        }
        catch (NumberFormatException nfe) {
            throw new CompilerException("EQU template hasn't been set up correctly.");
        }

        for (GlobalVariable gv : input.getGlobalVariables()) {
            int count = gv.getName().length();
            int tab = Math.max(1, space - count);

            String result = equ.replace("{$NAME", gv.getName())
                    .replaceAll("%[0-9]+\\}", Util.makeString(" ", tab))
                    .replace("{$VALUE}", gv.getPointer() + "");

            assembly.append(result);
            assembly.append("\n");
        }
    }

    /**
     * Writes the compiled output to the output file.
     */
    private void write() {
        new FileWorker(output).write(assembly.toString(), false);
    }

}
