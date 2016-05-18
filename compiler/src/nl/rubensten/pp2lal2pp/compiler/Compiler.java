package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.api.APIFunction;
import nl.rubensten.pp2lal2pp.lang.*;
import nl.rubensten.pp2lal2pp.lang.Number;
import nl.rubensten.pp2lal2pp.util.FileWorker;
import nl.rubensten.pp2lal2pp.util.Template;
import nl.rubensten.pp2lal2pp.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * The function the compiler is currently working with.
     */
    private Function function;

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

        // Initialisation: IOAREA
        assembly.append(Template.fillStatement("init:", "LOAD", Constants.REG_IOAREA, "IOAREA",
                "Store the address of the IOAREA for later use.\n"));

        // Initialisation: Global Variables.
        for (GlobalVariable gv : globalVariables) {
            if (R0.getIntValue() != ((Number)gv.getDefaultValue()).getIntValue()) {
                R0 = (Number)gv.getDefaultValue();

                assembly.append(Template.fillStatement("", "LOAD", Constants.REG_GENERAL, "" + gv
                        .getDefaultValue(), "Default value to load in global base.\n"));
            }

            assembly.append(Template.fillStatement("", "STOR", Constants.REG_GENERAL,
                    "[" + Constants.REG_GLOBAL_BASE + "+" + gv.getName() + "]",
                    "Give global variable " +  gv.getName() + " initial value " + gv
                            .getDefaultValue() + ".\n"));
        }

        assembly.append("\n");

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
        assembly.append("\n");

        // Used API functions
        if (input.getApiFunctions().parallelStream().anyMatch(p -> p.contains("7Segment"))) {
            assembly.append(Template.HEX7SEG.load());
            assembly.append("\n\n");
        }

        for (String string : input.getApiFunctions()) {
            if (string.equals("exit")) {
                continue;
            }

            Optional<Template> implementation = APIFunction.getImplementationTemplate(string);

            if (implementation.isPresent()) {
                assembly.append(implementation.get().load());
                assembly.append("\n");
            }
        }

        if (input.getApiFunctions().contains("exit")) {
            assembly.append(APIFunction.getImplementationTemplate("exit").get().load());
            assembly.append("\n");
        }

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
        this.function = function;

        for (String string : function.getPp2doc()) {
            assembly.append(";#  ").append(string).append("\n");
        }

        compileBlock(function.getContents(), function.getName(), Function.class);
        assembly.append("\n");
    }

    /**
     * Writes the given block to the assembly-StringBuilder.
     *
     * @param block
     *         The block to compile.
     * @param functionName
     *         The name of the function if it is the function's code block, or <code>null</code> if
     *         it is a nested block.
     * @param inside
     *         In what kind of element the block contains the contents of.
     */
    private void compileBlock(Block block, String functionName, Class inside) {
        List<Element> elts = block.getContents();
        String label = (functionName == null ? "" : functionName + ":");

        for (Element elt : elts) {
            // Function Continue
            if (elt instanceof Continue && functionName != null && inside != Loop.class) {
                compileFunctionContinue(label);
                label = "";
                continue;
            }

            // Function Return
            if (elt instanceof Return && functionName != null) {
                compileFunctionReturn((Return)elt, label);
                label = "";
                continue;
            }

            // Function call
            if (elt instanceof FunctionCall) {
                compileFunctionCall((FunctionCall)elt, label);
                label = "";
            }
        }
    }

    /**
     * Compiles a function call.
     *
     * @param call
     *         The functioncall element.
     * @param label
     *         The label to print before the statement.
     */
    private void compileFunctionCall(FunctionCall call, String label) {
        List<Variable> vars = call.getArguments();

        // API exit()
        if (call.getCalled().equals("exit")) {
            String stuff = Template.API_INVOKE_EXIT.load();

            if (label != null) {
                stuff = stuff.replaceAll("^" + Util.makeString(" ", label.length()), label);
            }

            assembly.append(stuff);
            assembly.append("\n");
            return;
        }

        for (Variable var : vars) {
            // If number, do direct stuff and things.
            if (var.isJustNumber()) {
                assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_GENERAL,
                        var.getDefaultValue().stringRepresentation(),
                        "Load the value " + var.getDefaultValue() +
                                " in the R0 to prepare it for the stack.\n"));
            }
            // Global variable
            else if (input.getGlobalVariable(var.getName()).isPresent()) {
                GlobalVariable gvar = input.getGlobalVariable(var.getName()).get();
                assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_GENERAL,
                        "[" + Constants.REG_GLOBAL_BASE + "+" + gvar.getName() + "]",
                        "Load the value of global variable " + gvar.getName() + ".\n"));
            }
            // Local variable
            else {
                assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_GENERAL,
                        "[" + Constants.REG_STACK_POINTER + "+" + var.getPointer() + "]",
                        "Load the value of variable " + var.getName() + ".\n"));
            }

            label = "";
            assembly.append(Template.fillStatement(label, "PUSH", Constants.REG_GENERAL, "",
                    "Push the value onto the stack.\n"));
        }

        assembly.append(Template.fillStatement(label, "BRS", call.getCalled(), "",
                "Call function " + call.getCalled() + ".\n"));

        assembly.append(Template.fillStatement("", "ADD", Constants.REG_STACK_POINTER,
                "" + vars.size(), "Reset the stack pointer position.\n"));
    }

    /**
     * Compiles a function return.
     *
     * @param ret
     *         The Return element.
     * @param label
     *         The label to print before the statement.
     */
    private void compileFunctionReturn(Return ret, String label) {
        // If there is a return value.
        if (ret.getReturnValue() != null) {
            assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_RETURN,
                    ret.getReturnValue().toString(), "Load the return value.\n"));
            label = "";
        }

        assembly.append(Template.fillStatement(label, "RTS", "", "",
                "Return from function " + function.getName() + "."));
    }

    /**
     * Compiles a function continue.
     *
     * @param label
     *         The label to print before the statement.
     */
    private void compileFunctionContinue(String label) {
        assembly.append(Template.fillStatement(label, "BRA", function.getName(), "",
                "Repeat function " + function.getName() + "."));
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
