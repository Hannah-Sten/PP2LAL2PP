package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.CompilerException;
import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.PP2LAL2PPException;
import nl.rubensten.pp2lal2pp.ParseException;
import nl.rubensten.pp2lal2pp.api.APIFunction;
import nl.rubensten.pp2lal2pp.lang.*;
import nl.rubensten.pp2lal2pp.lang.Number;
import nl.rubensten.pp2lal2pp.util.FileWorker;
import nl.rubensten.pp2lal2pp.util.Regex;
import nl.rubensten.pp2lal2pp.util.Template;
import nl.rubensten.pp2lal2pp.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
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
        compileDefinitions();
        assembly.append("\n");
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

            if (function instanceof Interrupt) {
                compileInterrupt((Interrupt)function);
            }
            else {
                compileFunction(function);
            }

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

        boolean hasReturn = function.getContents().getContents().parallelStream()
                .anyMatch(e -> (e instanceof Return) || (e instanceof Continue));

        compileBlock(function.getContents(), function.getName(), Function.class);

        if (!hasReturn) {
            String label = function.getContents().getContents().size() == 0 ? function.getName()
                    + ":" : "";
            compileFunctionReturn(new Return(), label);
        }

        assembly.append("\n");
    }

    /**
     * Writes the given interrupt function to the assembly-StringBuilder.
     *
     * @param interrupt
     *         The interrupt to compile.
     */
    private void compileInterrupt(Interrupt interrupt) {
        this.function = interrupt;

        assembly.append(Regex.replace(
                "{$ISRNAME}",
                Template.INTERRUPT_BOILERPLATE_BEFORE.replace("ISRNAME1", function.getName()),
                function.getName()
        )).append("\n\n");

        for (String string : function.getPp2doc()) {
            assembly.append(";#  ").append(string).append("\n");
        }

        compileBlock(function.getContents(), function.getName(), Interrupt.class);

        assembly.append("\n").append(Regex.replace(
                "{$ISRNAME}",
                Template.INTERRUPT_BOILERPLATE_AFTER.load(),
                function.getName()
        )).append("\n\n");
    }

    // Hihi
    private boolean functionReturn = false;

    /**
     * Writes the given block to the assembly-StringBuilder.
     *
     * @param block
     *         The block to compile.
     * @param functionName
     *         The name of the function if it is the function's code block, or <code>null</code> if
     *         it is a nested block. Or the label name.
     * @param inside
     *         In what kind of element the block contains the contents of.
     */
    private void compileBlock(Block block, String functionName, Class inside) {
        List<Element> elts = block.getContents();
        String label = (functionName == null ? "" : functionName + ":");
        List<Declaration> declarations = new ArrayList<>();

        boolean lastReturn = false;
        for (Element elt : elts) {
            lastReturn = false;

            // Function Continue
            if (elt instanceof Continue && inside != Loop.class) {
                if (inside == Interrupt.class) {
                    compileInterruptContinue(label);
                }
                else {
                    compileFunctionContinue(label);
                }
                label = "";
                lastReturn = true;
                continue;
            }

            // Function Return
            if (elt instanceof Return && (functionName != null || functionReturn)) {
                if (inside == Interrupt.class) {
                    compileInterruptReturn((Return)elt, label);
                }
                else {
                    compileFunctionReturn((Return)elt, label);
                }
                label = "";
                lastReturn = true;
                continue;
            }

            // Function call
            if (elt instanceof FunctionCall) {
                compileFunctionCall((FunctionCall)elt, label);
                label = "";
            }

            // Variable Declaration
            if (elt instanceof Declaration) {
                compileDeclaration((Declaration)elt, label);
                declarations.add((Declaration)elt);
                label = "";
            }

            // Operation
            if (elt instanceof Operation) {
                if (comment == null) {
                    operationComment = "Operation " + ((Operation)elt).toHumanReadableString() +
                            ".\n";
                }
                else {
                    operationComment = comment.getContents() + " {" + ((Operation)elt)
                            .toHumanReadableString() + "}\n";
                }
                compileOperation((Operation)elt, label);
                label = "";
            }

            // If-statement
            if (elt instanceof IfElse) {
                compileIfElse((IfElse)elt, label);
                label = "";
            }

            // Inject
            if (elt instanceof Inject) {
                compileInject((Inject)elt, label);
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

        if (declarations.size() > 0 && !lastReturn) {
            assembly.append(Template.fillStatement(label, "ADD", Constants.REG_STACK_POINTER,
                    declarations.size() + "",
                    "Reset stack pointer.\n"));

            for (Declaration decl : declarations) {
                Variable var = decl.getVariable();
                function.unregisterLocal(var);
            }
        }

        functionReturn = false;
    }

    /**
     * Compiles a declaration.
     *
     * @param declaration
     *         The declaration-object to compile.
     * @param label
     *         The label to punt in front of the first statement.
     */
    private void compileDeclaration(Declaration declaration, String label) {
        if (declaration.getScope() != Declaration.DeclarationScope.LOCAL) {
            return;
        }

        boolean call = false;
        if (declaration.getDeclaration() instanceof FunctionCall) {
            compileFunctionCall((FunctionCall)declaration.getDeclaration(), label);
            label = "";
            call = true;
        }

        Variable variable = declaration.getVariable();
        Value value = declaration.getDeclaration();
        String valArg = loadValueString(value);

        // Register variable.
        function.declareLocal(variable);

        String comment = this.comment == null ?
                "Load the initial value of " + variable.getName() + ". {declare " +
                        variable.getName() + "}\n" :
                this.comment.getContents() + " {declare " + variable.getName() + "}\n";

        if (!call) {
            assembly.append(Template.fillStatement(label, "LOAD", "R0", valArg,
                    comment));
        }

        assembly.append(Template.fillStatement("", "PUSH", call ? Constants.REG_RETURN : "R0", "",
                call ? comment : "Save the initial value of " + variable.getName() + ".\n"));
    }

    /**
     * Compiles an if-statement.
     *
     * @param ifElse
     *         The ifElse-statement to compile.
     * @param label
     *         The label to put in front of the first statement.
     */
    private void compileIfElse(IfElse ifElse, String label) {
        Operation operation = ifElse.getExpression();
        boolean functionCall = false;

        // No first element.
        if (operation == null) {
            throw new CompilerException("there is no operation (null)");
        }

        // Check for isInputOn call.
        if (operation.getFirstElement() instanceof FunctionCall) {
            compileFunctionCall((FunctionCall)operation.getFirstElement(), label);
            label = "";
            functionCall = true;
        }

        // Validity checks
        if (!operation.getSecondElement().isPresent() && !functionCall) {
            throw new CompilerException("expression " + operation.toHumanReadableString() +
                    " must have two values");
        }

        if (!operation.getOperator().isPresent() && !functionCall) {
            throw new CompilerException("expression " + operation.toHumanReadableString() +
                    " must have an operator");
        }

        if (!functionCall) {
            if (operation.getOperator().get().getType() != Operator.OperatorType.RELATIONAL) {
                throw new CompilerException("expression " + operation.toHumanReadableString() +
                        " must have a relational operator");
            }
        }

        Element first = operation.getFirstElement();
        Operator operator = functionCall ? Operator.GREATER_THAN : operation.getOperator().get();

        if (functionCall && operation.getOperator().isPresent()) {
            if (operation.getOperator().get() == Operator.BOOLEAN_NEGATION) {
                operator = Operator.LESSER_THAN_EQUAL;
            }
        }

        Element second = functionCall ? null : operation.getSecondElement().get();
        String instruction = operator.getInstruction().get();
        String prefix = "if" + ifElse.getId();

        // Comments
        String comment1 = "Check if " + operation.toHumanReadableString() +
                " (if-statement #" + ifElse.getId() + ").";
        String comment2 = ">";
        String comment3 = "Branch to if-block when " + operation.toHumanReadableString() + ".";

        if (comment != null) {
            comment2 = comment1;
            comment1 = comment.getContents();

            if (functionCall) {
                comment3 = comment1 + " (if-statement #" + ifElse.getId() + ").";
            }
        }

        // Compare
        if (!functionCall) {
            assembly.append(Template.fillStatement(label, "LOAD", "R0", loadValueString(first),
                    comment1 + "\n"));
            assembly.append(Template.fillStatement("", "CMP", "R0",
                    loadValueString(second), comment2 + "\n"));
        }

        // Branch to if-block.
        assembly.append(Template.fillStatement("", instruction, prefix + "_true", "",
                comment3 + "\n"));

        // Block if false (else)
        if (!ifElse.getElseBlock().getContents().isEmpty()) {
            functionReturn = true;
            compileBlock(ifElse.getElseBlock(), null, IfElse.class);
        }

        // Branch to skip if-block.
        assembly.append(Template.fillStatement("", "BRA", prefix + "_end", "",
                "Skip the if-block.\n"));

        // Block if true (if)
        if (ifElse.getIfBlock().getContents().size() > 0) {
            compileBlock(ifElse.getIfBlock(), prefix + "_true", IfElse.class);
        }
        else {
            assembly.append(Template.fillStatement(prefix + "_true:", "LOAD", "R0", "0",
                    "Dummy instruction to always make the label work.\n"));
        }

        // The end.
        assembly.append(Template.fillStatement(prefix + "_end:", "LOAD", "R0", "0",
                "Dummy instruction to always make the label work.\n"));
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
            if (EnumSet.of(Operator.ASSIGN, Operator.ASSIGN_ALT_LEFT, Operator.ASSIGN_ALT_RIGHT)
                    .contains(op.getOperator().get())) {

                if (op.getOperator().get() == Operator.ASSIGN_ALT_RIGHT) {
                    op.swap();
                }

                if (op.getSecondElement().isPresent()) {
                    Element first = op.getFirstElement();
                    Element second = op.getSecondElement().get();

                    // When first evaluating operation, then assign.
                    if (second instanceof Operation) {
                        Operation secondOp = (Operation)second;
                        if (secondOp.getOperator().isPresent()) {
                            compileOperation((Operation)second, operationLabel);

                            if (first instanceof Variable) {
                                Variable var = (Variable)first;

                                if (input.getGlobalVariable(var.getName()).isPresent()) {
                                    assembly.append(Template.fillStatement("", "STOR", "R0", "[" +
                                                    Constants.REG_GLOBAL_BASE + "+" + var.getName() + "]",
                                            operationComment));
                                }
                                else {
                                    int pointer = function.getVariableByVariable(var).getPointer();
                                    assembly.append(Template.fillStatement("", "STOR", "R0", "[" +
                                                    Constants.REG_STACK_POINTER + "+" + pointer +
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
                        else if (secondOp.getFirstElement() instanceof FunctionCall) {
                            compileFunctionCall((FunctionCall)secondOp.getFirstElement(), operationLabel);
                            operationLabel = "";
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
                else if (op.getFirstElement() instanceof FunctionCall) {

                    if (op.getFirstElement() instanceof FunctionCall) {
                        compileFunctionCall((FunctionCall)op.getFirstElement(), operationLabel);
                        operationLabel = "";
                    }
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
        if (second instanceof FunctionCall) {
            compileFunctionCall((FunctionCall)second, operationLabel);
            operationLabel = "";
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
            Operation operation = (Operation)element;
            if (operation.getFirstElement() instanceof FunctionCall) {
                return Constants.REG_RETURN;
            }

            return ((Number)operation.getFirstElement()).stringRepresentation();
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

            int pointer = function.getVariableByVariable(var).getPointer();
            return "[" + Constants.REG_STACK_POINTER + "+" + pointer + "]";
        }

        if (element instanceof FunctionCall) {
            return Constants.REG_RETURN;
        }

        if (element instanceof Value) {
            Value val = (Value)element;

            Optional<GlobalVariable> global = input.getGlobalVariable(val.stringRepresentation());
            if (global.isPresent()) {
                return loadValueString(global.get());
            }

            Variable var = function.getVariableByName(val.stringRepresentation());
            return loadValueString(var);
        }

        throw new ParseException("the element '" + element + "' must be either a Number or " +
                "Variable");
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
            int pointer = function.getVariableByVariable(var).getPointer();
            return "[" + Constants.REG_STACK_POINTER + "+" + pointer + "]";
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
        // API set7Segment(dig, pattern)
        else if (call.getCalled().equals("set7Segment")) {
            Variable arg1 = vars.get(0);
            Variable arg2 = vars.get(1);

            String textArg1 = getVariableValue(arg1);
            String textArg2 = getVariableValue(arg2);

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
        // API getNumPattern(val)
        else if (call.getCalled().equals("getNumPattern")) {
            Variable arg = vars.get(0);
            String text = getVariableValue(arg);

            String result = insertLabel(Template.API_INVOKE_GETNUMPATTERN.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Get the 7Segment pattern for " + text + ".\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
        }
        // API getPattern(val)
        else if (call.getCalled().equals("getPattern")) {
            Variable arg = vars.get(0);
            String text = getVariableValue(arg);

            String result = insertLabel(Template.API_INVOKE_GETPATTERN.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Get the 7Segment pattern for " + text + ".\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
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

            String result = Template.API_INVOKE_SETSINGLEOUTPUT.replace(
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
        // API getInputStates()
        else if (call.getCalled().equals("getAnalogStates")) {
            String result = insertLabel(Template.API_INVOKE_GETANALOGSTATES.load(), label);
            assembly.append(result);
            assembly.append("\n");
            skipVariables = true;
            skipCall = true;
            label = "";
        }
        // API getAnalog(num)
        else if (call.getCalled().equals("getAnalog")) {
            Variable arg = vars.get(0);
            String text = getVariableValue(arg);

            String result = insertLabel(Template.API_INVOKE_GETANALOG.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Set what A/D-converter value has to be loaded.\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
        }
        // API getTimer()
        else if (call.getCalled().equals("getTimer")) {
            String result = insertLabel(Template.API_INVOKE_GETTIMER.load(), label);
            assembly.append(result);
            assembly.append("\n");
            skipVariables = true;
            skipCall = true;
            label = "";
        }
        // API addTimer(val)
        else if (call.getCalled().equals("addTimer")) {
            Variable arg = vars.get(0);
            String text = getVariableValue(arg);

            String result = insertLabel(Template.API_INVOKE_ADDTIMER.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Set what value has to be added to the timer.\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
        }
        // API setTimer(val)
        else if (call.getCalled().equals("setTimer")) {
            Variable arg = vars.get(0);
            String text = getVariableValue(arg);

            String result = insertLabel(Template.API_INVOKE_SETTIMER.replace(
                    "ARG", text
            ), label)
                    .replace("{$COMMENT}", "Set to what value the timer has to be set.\n");
            assembly.append(result);
            skipVariables = true;
            label = "";
        }
        // Semi-API enableTimerInterrupt(interruptName)
        else if (call.getCalled().equals("enableTimerInterrupt")) {
            String interruptName = vars.get(0).getName();
            String result = insertLabel(Template.ENABLE_INTERRUPT.replace(
                    "ISRNAME", interruptName
            ), label).replace(
                    "{$COMMENT}", "Enable the interrupt " + interruptName + ".\n"
            );

            assembly.append(result);
            skipVariables = true;
            skipCall = true;
            label = "";
        }
        // Semi-API disableTimerInterrupt(interruptName)
        else if (call.getCalled().equals("disableTimerInterrupt")) {
            String interruptName = vars.get(0).getName();
            String result = insertLabel(Template.DISABLE_INTERRUPT.replace(
                    "ISRNAME", interruptName
            ), label).replace(
                    "{$COMMENT}", "Disable the interrupt " + interruptName + ".\n"
            );
            ;

            assembly.append(result);
            skipVariables = true;
            skipCall = true;
            label = "";
        }

        // Reverse loop, as the first argument must be pushed last.
        for (int i = vars.size() - 1; i >= 0; i--) {
            Variable var = vars.get(i);

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
                int pointer = function.getVariableByVariable(var).getPointer();
                assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_GENERAL,
                        "[" + Constants.REG_STACK_POINTER + "+" + pointer + "]",
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

        if (!skipVariables && vars.size() > 0) {
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
        // Return statements without values.
        if (ret instanceof ElementReturn) {
            ElementReturn eltReturn = (ElementReturn)ret;
            Element element = eltReturn.getElement();

            // Function call
            if (element instanceof FunctionCall) {
                FunctionCall call = (FunctionCall)element;
                compileFunctionCall(call, label);

                resetStackPointer("");

                assembly.append(Template.fillStatement("", "RTS", "", "",
                        "Return from function " + function.getName() + ".\n"));
                return;
            }
            // Variable
            if (element instanceof Variable) {
                Variable var = (Variable)element;
                assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_RETURN,
                        loadValueString(var),
                        "Load the value of variable " + var.getName() + " as return value.\n"));

                resetStackPointer("");

                assembly.append(Template.fillStatement("", "RTS", "", "",
                        "Return from function " + function.getName() + ".\n"));
                return;
            }
        }

        // If there is a return value.
        if (ret.getReturnValue() != null) {
            assembly.append(Template.fillStatement(label, "LOAD", Constants.REG_RETURN,
                    ret.getReturnValue().toString(), "Load the return value.\n"));
            label = "";
        }

        // Reset stack pointer.
        if (resetStackPointer(label)) {
            label = "";
        }

        assembly.append(Template.fillStatement(label, "RTS", "", "",
                "Return from function " + function.getName() + ".\n"));
    }

    /**
     * Undos the stack pointer change of the declaration of local variables.
     *
     * @param label
     *         The label to put in front of the statement.
     * @return <code>true</code> if there is a change, <code>false</code> if there is no change in
     * the stack pointer.
     */
    private boolean resetStackPointer(String label) {
        if (function.variableCount() > 0) {
            assembly.append(Template.fillStatement(label, "ADD", Constants.REG_STACK_POINTER,
                    function.variableCount() + "",
                    "Reset stack pointer.\n"));
            return true;
        }

        return false;
    }

    /**
     * Compiles a function return.
     *
     * @param ret
     *         The Return element.
     * @param label
     *         The label to print before the statement.
     */
    private void compileInterruptReturn(Return ret, String label) {
        // Return statements without values.
        if (ret.getReturnValue() != null) {
            throw new PP2LAL2PPException("Interrupt cannot return a value");
        }

        // Reset stack pointer.
        if (function.variableCount() > 0) {
            assembly.append(Template.fillStatement(label, "ADD", Constants.REG_STACK_POINTER,
                    function.variableCount() + "",
                    "Reset stack pointer.\n"));
            label = "";
        }

        assembly.append(Template.fillStatement(label, "RTE", "", "",
                "Return from interrupt " + function.getName() + ".\n"));
    }

    /**
     * Compiles a function continue.
     *
     * @param label
     *         The label to print before the statement.
     */
    private void compileFunctionContinue(String label) {
        // Reset stack pointer.
        if (function.variableCount() > 0) {
            assembly.append(Template.fillStatement(label, "ADD", Constants.REG_STACK_POINTER,
                    function.variableCount() + "",
                    "Reset stack pointer.\n"));
            label = "";
        }

        assembly.append(Template.fillStatement(label, "BRA", function.getName(), "",
                "Repeat function " + function.getName() + ".\n"));
    }

    /**
     * Compiles a interrupt continue.
     *
     * @param label
     *         The label to print before the statement.
     */
    private void compileInterruptContinue(String label) {
        // Reset stack pointer.
        if (function.variableCount() > 0) {
            assembly.append(Template.fillStatement(label, "ADD", Constants.REG_STACK_POINTER,
                    function.variableCount() + "",
                    "Reset stack pointer.\n"));
            label = "";
        }

        assembly.append(Template.fillStatement(label, "BRS", "enable_" + function.getName(), "", "Re-enable interrupt.\n"));

        assembly.append(Template.fillStatement("", "RTE", "", "",
                "Return from interrupt " + function.getName() + ".\n"));
    }

    /**
     * Writes all the definitions to the assembly-StringBuilder.
     */
    private void compileDefinitions() {
        for (Definition def : input.getDefinitions()) {
            String comment = "Define " + def.getName() + " as value " + def.getValue()
                    .stringRepresentation();

            Optional<String> docString = def.getDocString();
            if (docString.isPresent()) {
                comment = docString.get();
            }

            assembly.append(Template.EQU.replace("NAME", def.getName(), "VALUE",
                    def.getValue().stringRepresentation()).replace("{$COMMENT}", comment));
            assembly.append("\n");
        }
    }

    /**
     * Writes all the global variables of the program to the assembly-StringBuilder.
     */
    private void compileGlobal() {
        for (GlobalVariable gv : input.getGlobalVariables()) {
            assembly.append(Template.EQU.replace("NAME", gv.getName(), "VALUE", gv.getPointer() +
                    "").replace("{$COMMENT}",
                    (gv.getComment() == null ? "" : gv.getComment().getContents())));
            assembly.append("\n");
        }
    }

    /**
     * Compiles an inject statement. Tries to format the raw assembly.
     *
     * @param inject
     *         The Inject element.
     * @param label
     *         The label to print before the statement.
     */
    private void compileInject(Inject inject, String label) {
        String inj_comment = null;

        if (comment != null) {
            inj_comment = comment.getContents();
        }

        for (String line : inject.getContents().split("\n")) {
            assembly.append(Template.fillStatement(label, line, "", "", inj_comment));
            assembly.append("\n");
            label = "";
            inj_comment = null;
        }
    }

    /**
     * Writes the compiled output to the output file.
     */
    private void write() {
        new FileWorker(output).write(assembly.toString(), false);
    }

}
