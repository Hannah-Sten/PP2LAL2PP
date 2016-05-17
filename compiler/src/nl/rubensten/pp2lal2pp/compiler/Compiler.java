package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.lang.*;
import nl.rubensten.pp2lal2pp.lang.Number;
import nl.rubensten.pp2lal2pp.util.FileWorker;
import nl.rubensten.pp2lal2pp.util.Template;

import java.io.File;
import java.util.ArrayList;
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

        // Initialisation
        assembly.append(";#\n;#  Initialisation of the program.\n;#\n");
        String label = "init:";
        Number R0 = Number.MINUS_ONE;
        List<GlobalVariable> globalVariables = new ArrayList<>(input.getGlobalVariables());
        globalVariables.sort((c1, c2) ->
                Integer.valueOf(
                        ((Number)c1.getDefaultValue()).getIntValue()).compareTo(
                        ((Number)c2.getDefaultValue()).getIntValue()));

        // Initialisation: Global Variables.
        for (GlobalVariable gv : globalVariables) {
            if (R0 != gv.getDefaultValue()) {
                R0 = (Number)gv.getDefaultValue();

                assembly.append(
                        Template.STATEMENT.replace(
                            "LABEL", label,
                            "INSTRUCTION", "LOAD",
                            "ARG1", "R0",
                            "ARG2", "" + gv.getDefaultValue())
                            .replace("{$COMMENT}", "; Default value to load in global base."))
                        .append("\n");
            }

            assembly.append(
                    Template.STATEMENT.replace(
                            "LABEL", "",
                            "INSTRUCTION", "STOR",
                            "ARG1", "R0",
                            "ARG2", "[GB+" + gv.getName() + "]")
                            .replace("{$COMMENT}", "; Give global variable " +
                                    gv.getName() + " initial value " + gv.getDefaultValue() + "."))
                    .append("\n");

            label = "";
        }

        // Initialisation: IOAREA
        assembly.append(
                Template.STATEMENT.replace(
                        "LABEL", "",
                        "INSTRUCTION", "LOAD",
                        "ARG1", Constants.REG_IOAREA,
                        "ARG2", "IOAREA")
                        .replace("{$COMMENT}", "; Store the address of the IOAREA for later use."))
                .append("\n\n");

        // Main function
        compileFunction(input.getMainFunction());

        // Other functions
        for (Function function : input.getFunctions()) {
            if (function.equals(input.getMainFunction())) {
                continue;
            }

            assembly.append("\n");
            compileFunction(function);
        }

        // Used API functions


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

            assembly.append(";#  ").append(line);
        }
    }

    /**
     * Writes the given function to the assembly-StringBuilder.
     *
     * @param function
     *         The function to compile.
     */
    private void compileFunction(Function function) {
        for (String string : function.getPp2doc()) {
            assembly.append(";#  ").append(string).append("\n");
        }

        String result = Template.STATEMENT.replace("LABEL", function.getName() + ":");

        // First statement
        List<Element> contents = function.getContents().getContents();

        assembly.append(result);
        assembly.append("\n");
    }

    /**
     * Writes all the global variables of the program to the assembly-StringBuilder.
     */
    private void compileGlobal() {
        for (GlobalVariable gv : input.getGlobalVariables()) {

            assembly.append(Template.EQU.replace("NAME", gv.getName(), "VALUE", gv.getPointer() +
                    "").replace("{$COMMENT}", gv.getComment().getContents()));
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
