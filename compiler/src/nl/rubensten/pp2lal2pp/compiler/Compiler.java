package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.ParseException;
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

    /**
     * The latest comment the compiler has processed.
     */
    private Comment comment;

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
                    "Give global variable " + gv.getName() + " initial value " + gv
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
                assembly.append("\n\n");
            }
        }

        if (input.getApiFunctions().contains("exit")) {
            assembly.append(APIFunction.getImplementationTemplate("exit").get().load());
            assembly.append("\n\n");
        }

        // @END
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

            // Operation
            if (elt instanceof Operation) {
                if (comment == null) {
                    operationComment = "Operation " + ((Operation)elt).toHumanReadableString() + "\n";
                }
                else {
                    operationComment = comment.getContents() + " {" + ((Operation)elt)
                            .toHumanReadableString() + "}\n";
                }
                compileOperation((Operation)elt, label);
                label = "";
            }

            // Comment
            if (elt instanceof Comment) {
                this.comment = (Comment)elt;
            }
            else {
                this.comment = null;
            }
        }
    }

    // Hihi
    private String operationLabel;
    private String operationComment;

    /**
     * Compiles an operation statement.
     *
     * @param op
     *         The operation to compile.
     * @param label
     *         The label to put in front of the first statement.
     */
    private void compileOperation(Operation op, String label) {
        operationLabel = label;

        if (op.getOperator().isPresent()) {
            if (op.getOperator().get() == Operator.ASSIGN) {
                if (op.getSecondElement().isPresent()) {
                    Element second = op.getSecondElement().get();

                    // When first evaluating operation, then assign.
                    if (second instanceof Operation) {
                        if (((Operation)second).getOperator().isPresent()) {
                            compileOperation((Operation)second, operationLabel);

                            if (op.getFirstElement() instanceof Variable) {
                                Variable var = (Variable)op.getFirstElement();

                                if (input.getGlobalVariable(var.getName()).isPresent()) {
                                    assembly.append(Template.fillStatement("", "STOR", "R0", "[" +
                                            Constants.REG_GLOBAL_BASE + "+" + var.getName() + "]",
                                            operationComment));
                                }
                                else {
                                    assembly.append(Template.fillStatement("", "STOR", "R0", "[" +
                                            Constants.REG_STACK_POINTER + "+" + var.getPointer() +
                                            "]",
                                            operationComment));
                                }
                            }
                            else {
                                throw new CompilerException("you can only assign a value to " +
                                        "variables");
                            }

                            operationComment = ">\n";

                            return;
                        }
                    }

                    if (op.getFirstElement() instanceof Number) {
                        throw new CompilerException("can't assign a value to a Number");
                    }

                    if (!op.getOperator().isPresent()) {
                        second = new Number(((Number)op.getFirstElement()).getIntValue());
                    }

                    // Simple a=34 assignment.
                    assembly.append(Template.fillStatement(operationLabel, "LOAD", "R0",
                            loadValueString(second), operationComment));
                    operationLabel = "";
                    assembly.append(Template.fillStatement(operationLabel, "STOR", "R0",
                            loadValueString(op.getFirstElement()), ">\n"));
                    operationComment = ">\n";

                    return;
                }
            }
        }

        if (op.getFirstElement() instanceof Operation) {
            compileOperation((Operation)op.getFirstElement(), operationLabel);
        }

        if (op.getSecondElement().isPresent()) {
            if (op.getSecondElement().get() instanceof Operation) {
                compileOperation((Operation)op.getSecondElement().get(), operationLabel);
                return;
            }
        }

        Element first = op.getFirstElement();

        if (!op.getOperator().isPresent()) {
            return;
        }

        Operator operator = op.getOperator().get();

        if (!op.getSecondElement().isPresent()) {
            return;
        }

        Element second = op.getSecondElement().get();

        // If function call.
        boolean functionCall = false;
        if (second instanceof FunctionCall) {
            compileFunctionCall((FunctionCall)first, operationLabel);
            operationLabel = "";
            functionCall = true;
        }

        assembly.append(Template.fillStatement(operationLabel, "LOAD", "R0", loadValueString(first),
                operationComment));
        operationLabel = "";
        if (operator.getInstruction().isPresent()) {
            assembly.append(Template.fillStatement(operationLabel, operator.getInstruction().get(),
                    "R0", loadValueString(second), ">\n"));
        }

        if (operator.getType() == Operator.OperatorType.ASSIGNMENT) {
            assembly.append(Template.fillStatement(operationLabel, "STOR", "R0",
                    loadValueString(first), ">\n"));
        }

        operationComment = ">\n";
    }

    /**
     * Get what you have to load. Bit weird sentence I know. Let me elaborate a little bit. If you
     * want to load something to R0 for example you use <code>LOAD R0 [something]</code>. Well. This
     * method determines what that something is!
     *
     * @param element
     *         The element to get the something of.
     * @return The something.
     */
    private String loadValueString(Element element) {
        if (element instanceof Operation) {
            return ((Number)((Operation)element).getFirstElement()).stringRepresentation();
        }

        if (element instanceof Number) {
            return ((Number)element).stringRepresentation();
        }

        if (element instanceof Variable) {
            Variable var = (Variable)element;

            if (input.getGlobalVariable(var.getName()).isPresent()) {
                return "[" + Constants.REG_GLOBAL_BASE + "+" + var.getName() +
                        "]";
            }

            return "[" + Constants.REG_STACK_POINTER + "+" + var.getPointer() + "]";
        }

        throw new ParseException("the element must be either a Number or Variable");
    }

    /**
     * Inserts a label if the label does not equals <code>null</code>.
     */
    private String insertLabel(String stuff, String label) {
        if (label != null) {
            stuff = stuff.replaceAll("^" + Util.makeString(" ", label.length()), label);
        }

        return stuff;
    }

    /**
     * Get the way the value of the variable should be gotten in a load statement.
     * <p>
     * E.g. for a number it would be the decimal representation. However, for global variables it
     * would be <code>[GB+POINTER]</code>.
     * <p>
     * Props for the amazing docs!
     *
     * @param var
     *         The variable to get the value-thingy from.
     * @return The value-thingy.
     */
    private String getVariableValue(Variable var) {
        // Just number
        if (var.isJustNumber()) {
            return var.getDefaultValue().toString();
        }
        // Arg1: Global variable
        else if (input.getGlobalVariable(var.getName()).isPresent()) {
            return "[" + Constants.REG_GLOBAL_BASE + "+" + var.getName() + "]";
        }
        // Arg1: Local variable
        else {
            return "[" + Constants.REG_STACK_POINTER + "+" + var.getPointer() + "]";
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
        boolean skipVariables = false;
        boolean skipCall = false;

        // API exit()
        if (call.getCalled().equals("exit")) {
            assembly.append(insertLabel(Template.API_INVOKE_EXIT.load(), label));
            assembly.append("\n");
            return;
        }
        // API set7Segment(dig, val)
        else if (call.getCalled().equals("set7Segment")) {
            Variable arg1 = vars.get(0);
            Variable arg2 = vars.get(1);

            String textArg1 = getVariableValue(arg1);
            String textArg2 = getVariableValue(arg2);

            if (arg2.isJustNumber()) {
                textArg2 = "%" + Integer.toBinaryString(Integer.parseInt(textArg2));
            }

            String result = Template.API_INVOKE_SET7SEGMENT.replace(
                    "ARG1", textArg1,
                    "ARG2", textArg2)
                    .replace("{$COMMENT1}", "Load the value to display on the 7Segment display.")
                    .replace("{$COMMENT2}", "Load the index of the display on the 7Segment " +
                            "display.");

            String stuff = insertLabel(result, label);
            assembly.append(stuff).append("\n");

            label = "";
            skipVariables = true;
        }
        // API getInputStates()
        else if (call.getCalled().equals("getInputStates")) {
            String result = insertLabel(Template.API_INVOKE_GETINPUTSTATES.load(), label);
            assembly.append(result);
            assembly.append("\n");
            skipVariables = true;
            skipCall = true;
            label = "";
        }
        // API isInputOn(num)
        else if (call.getCalled().equals("isInputOn")) {
            String text = getVariableValue(vars.get(0));
            if (vars.get(0).isJustNumber()) {
                text = "%" + Integer.toBinaryString(Integer.parseInt(text));
            }

            String result = insertLabel(Template.API_INVOKE_ISINPUTON.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Load the number of the input to check for.\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
        }
        // API setOutput(val)
        else if (call.getCalled().equals("setOutput")) {
            Variable arg = vars.get(0);
            String text = getVariableValue(arg);
            if (arg.isJustNumber()) {
                text = "%" + Integer.toBinaryString(Integer.parseInt(text));
            }

            String result = insertLabel(Template.API_INVOKE_SETOUTPUT.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Set what value has to be loaded into output.\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
        }
        // API setSingleOutput(num, val)
        else if (call.getCalled().equals("setSingleOutput")) {
            Variable arg1 = vars.get(0);
            Variable arg2 = vars.get(1);

            String textArg1 = getVariableValue(arg1);
            String textArg2 = getVariableValue(arg2);

            if (arg1.isJustNumber()) {
                textArg1 = "%" + Integer.toBinaryString(Integer.parseInt(textArg1));
            }

            String result = Template.API_INVOKE_SET7SEGMENT.replace(
                    "ARG1", textArg1,
                    "ARG2", textArg2)
                    .replace("{$COMMENT1}", "Load the index of the output to change.")
                    .replace("{$COMMENT2}", "Command to turn the output " + (textArg2.equals("0")
                            ? "off" : "on") + ".");

            String stuff = insertLabel(result, label);
            assembly.append(stuff).append("\n");

            label = "";
            skipVariables = true;
        }

        for (Variable var : vars) {
            if (skipVariables) {
                break;
            }

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

        if (!skipCall) {
            assembly.append(Template.fillStatement(label, "BRS", call.getCalled(), "",
                    "Call function " + call.getCalled() + ".\n"));
        }

        if (!skipVariables) {
            assembly.append(Template.fillStatement("", "ADD", Constants.REG_STACK_POINTER,
                    "" + vars.size(), "Reset the stack pointer position.\n"));
        }
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
