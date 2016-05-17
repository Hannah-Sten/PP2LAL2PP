package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.lang.Element;
import nl.rubensten.pp2lal2pp.lang.Function;
import nl.rubensten.pp2lal2pp.lang.GlobalVariable;
import nl.rubensten.pp2lal2pp.lang.Program;
import nl.rubensten.pp2lal2pp.util.FileWorker;
import nl.rubensten.pp2lal2pp.util.Template;
import nl.rubensten.pp2lal2pp.util.Util;

import java.io.File;
import java.util.List;

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
        // Comment header.
        compileHeader();
        assembly.append("\n");

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
        compileFunction(input.getMainFunction());

        // Other functions


        // @END
        assembly.append("\n");
        assembly.append(Template.END);

        // Write to file
        write();
    }

    /**
     * Nicely prints the header to the file.
     */
    private void compileHeader() {
        if (input.getHeader().size() <= 0) {
            return;
        }

        boolean first = true;
        for (String line : input.getHeader()) {
            if (!first) {
                assembly.append("\n");
            }
            first = false;

            assembly.append(";# ").append(line);
        }
    }

    /**
     * Get the amount of characters of space is reserved for the given variable idk.
     */
    private int getSpace(String source, String name) {
        try {
            return Integer.parseInt(source.replaceAll(".*\\{\\$" + name + "%", "")
                    .replaceAll("\\}.*", ""));
        }
        catch (NumberFormatException nfe) {
            throw new CompilerException("EQU template hasn't been set up correctly ({$" +
                    name + "%#}).");
        }
    }

    /**
     * Writes the given function to the assembly-StringBuilder.
     *
     * @param function
     *         The function to compile.
     */
    private void compileFunction(Function function) {
        String line = Template.STATEMENT.load();

        int spaceLabel = getSpace(line, "LABEL");
        int spaceInstruction = getSpace(line, "INSTRUCTION");
        int spaceArg1 = getSpace(line, "ARG1");
        int spaceArg2 = getSpace(line, "ARG2");

        // Label
        int labelLength = function.getName().length() + 1;
        int labelFill = Math.max(1, spaceLabel - labelLength);

        String result = line.replace("{$LABEL", function.getName() + ":")
                .replaceFirst("%[0-9]+\\}", Util.makeString(" ", labelFill));

        // First statement
        List<Element> contents = function.getContents().getContents();

        assembly.append(result);
        assembly.append("\n");
    }

    /**
     * Writes all the global variables of the program to the assembly-StringBuilder.
     */
    private void compileGlobal() {
        String equ = Template.EQU.load();

        int spaceName = getSpace(equ, "NAME");
        int spaceValue = getSpace(equ, "VALUE");

        for (GlobalVariable gv : input.getGlobalVariables()) {
            int countName = gv.getName().length();
            int countValue = Integer.toString(gv.getPointer()).length();
            int tabName = Math.max(1, spaceName - countName);
            int tabValue = Math.max(1, spaceValue - countValue);

            String result = equ.replace("{$NAME", gv.getName())
                    .replaceFirst("%[0-9]+\\}", Util.makeString(" ", tabName))
                    .replace("{$VALUE", gv.getPointer() + "")
                    .replaceFirst("%[0-9]+\\}", Util.makeString(" ", tabValue))
                    .replace("{$COMMENT}", gv.getComment().getContents());

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
