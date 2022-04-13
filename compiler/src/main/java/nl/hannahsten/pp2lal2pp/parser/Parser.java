package nl.hannahsten.pp2lal2pp.parser;

import nl.hannahsten.pp2lal2pp.ParseException;
import nl.hannahsten.pp2lal2pp.api.APIFunction;
import nl.hannahsten.pp2lal2pp.lang.Number;
import nl.hannahsten.pp2lal2pp.lang.*;
import nl.hannahsten.pp2lal2pp.util.Regex;
import nl.hannahsten.pp2lal2pp.util.Util;

import java.util.*;

/**
 * Takes one big code string and parses it to a {@link Program}.
 * <p>
 * This class does not handle inclusions and definitions.
 *
 * @author Hannah Schellekens
 */
public class Parser {

    /**
     * The complete string of code to parse.
     */
    String input;

    /**
     * The program that gets parsed.
     */
    private Program program;

    /**
     * The name of the function that is currently being parsed.
     */
    private String currentFunction;

    /**
     * The last comment covered.
     */
    private Comment lastComment;

    Parser(String input) {
        this.input = input;
    }

    /**
     * Parses the complete input.
     */
    public Program parse() throws ParseException {
        program = new Program();
        program.setHeader(parseHeaderComment(input));

        Function function;

        LineTokeniser lines = new LineTokeniser(input);
        List<String> pp2doc = new ArrayList<>();

        for (Iterator<String> it = lines.iterator(); it.hasNext(); ) {
            String line = it.next();
            Tokeniser tokens = new Tokeniser(line);

            // Parse global variables.
            if (parseGlobal(program, tokens)) {
                pp2doc.clear();
                continue;
            }

            // Comments
            if (line.startsWith("#")) {
                String comment = Regex.replaceAll("^#;?\\s*", line, "");
                lastComment = new Comment(comment);
                pp2doc.add(comment);
                continue;
            }
            // Define
            else if (line.startsWith("define")) {
                program.addDefinition(parseDefine(new Tokeniser(line)));
                continue;
            }

            boolean isInterrupt = line.startsWith("interrupt");

            if (!line.startsWith("function") && !isInterrupt) {
                continue;
            }

            // Parse function.
            currentFunction = tokens.getToken(1);
            List<Variable> arguments = new ArrayList<>();

            if (!tokens.equals(2, "(")) {
                throw new ParseException("Function " + currentFunction + " must be followed by " +
                        "parentheses.");
            }

            for (int i = 3; i <= tokens.size(); i++) {
                if (isInterrupt) {
                    break;
                }

                if (tokens.equals(i, ")")) {
                    break;
                }

                if (tokens.equals(i, ",")) {
                    continue;
                }

                String name = tokens.getToken(i);
                if (arguments.parallelStream().anyMatch(v -> v.getName().equals(name))) {
                    throw new ParseException("Function " + currentFunction + " can't have two of " +
                            "the same argument names (" + name + ").");
                }

                arguments.add(new Variable(name));
            }

            if (isInterrupt) {
                function = new Interrupt(currentFunction, new ArrayList<>(pp2doc));
            } else {
                function = new Function(currentFunction, new ArrayList<>(pp2doc), arguments);
            }

            pp2doc.clear();
            Block block = parseFunction(it);
            function.setContents(block);
            program.addFunction(function);
        }

        return program;
    }

    /**
     * Parses the contents of a function block.
     *
     * @param lines
     *         The iterator of the LineTokeniser.
     * @return The block of code for the given function.
     */
    private Block parseFunction(Iterator<String> lines) {
        List<Element> body = new ArrayList<>();

        while (lines.hasNext()) {
            Tokeniser line = new Tokeniser(lines.next());
            StringBuilder comment = new StringBuilder();
            boolean parsed = false;

            // Function is done.
            if (line.isFirst("}")) {
                break;
            }

            // Variable declarations.
            if (line.isFirst("var")) {
                body.add(parseVariableDeclaration(line));
                parsed = true;
            }
            // Loops
            else if (line.isFirst("loop")) {
                body.add(parseLoop(lines, line));
                parsed = true;
            }
            // If-Else statements.
            else if (line.isFirst("if")) {
                body.add(parseIfElse(lines, line));
                parsed = true;
            }
            // Function calls.
            else if (isFunctionCall(line)) {
                body.add(parseFunctionCall(line));
                parsed = true;
            }
            // Continue
            else if (line.isFirst("continue")) {
                body.add(new Continue());
                parsed = true;
            }
            // Return
            else if (line.isFirst("return")) {
                body.add(parseReturn(line));
                parsed = true;
            }
            // Full line comment.
            else if (line.equals(0, "#")) {
                parsed = true;
            }
            // Inject raw assembly.
            else if (line.isFirst("inject")) {
                body.add(parseInject(lines, line));
                parsed = true;
            }

            // Anything else.
            if (!parsed && !line.isFirstIgnore("else", "}")) {
                Operation op = parseOperation(line.iterator(), line);

                if (op != null) {
                    body.add(op);
                }
            }

            // Comments
            for (String token : line) {
                // Comments
                if (token.equals("#")) {
                    comment.append(" ");
                    continue;
                }

                if (comment.length() > 0) {
                    comment.append(token).append(" ");
                }
            }

            if (comment.length() > 0) {
                body.add(new Comment(comment.toString().trim()));
            }
        }

        return new Block(body);
    }

    /**
     * @return The parsed program if the {@link Parser#parse()} method has been called.
     * @throws ParseException
     *         if the parse method has not been called before.
     */
    public Program getProgram() throws ParseException {
        if (program == null) {
            throw new ParseException("parse() has not been called.");
        }

        return program;
    }

    /**
     * Parses a definition.
     *
     * @param line
     *         The line the definition is on.
     * @return The Definition-object.
     */
    private Definition parseDefine(Tokeniser line) {
        if (line.size() != 3) {
            throw new ParseException("illegal definition at line '" + line.getOriginal() + "'");
        }

        String comment = (lastComment == null ? "" : lastComment.getContents());
        String name = line.getToken(1);
        Value value = Value.parse(line.getToken(2), program);

        if (!(value instanceof nl.hannahsten.pp2lal2pp.lang.Number)) {
            throw new ParseException("wrong number format at definition '" +
                    line.getOriginal() + "");
        }

        return new Definition(name, value, comment);
    }

    /**
     * Parses a return statement.
     *
     * @param line
     *         The line the return statement is on.
     * @return The return object.
     */
    private Return parseReturn(Tokeniser line) {
        if (line.size() == 1) {
            return new Return();
        }

        if (line.size() >= 3) {
            if (line.equals(2, "(") && !line.equals(1, "'")) {
                // Function call.
                String name = line.getToken(1);
                List<Variable> variables = new ArrayList<>();

                if (!line.equals(3, ")")) {
                    for (int i = 3; i < line.sizeNoComments(); i += 2) {
                        Value val = Value.parse(line.getToken(i), program);

                        if (val instanceof nl.hannahsten.pp2lal2pp.lang.Number) {
                            variables.add(new Variable("num", val).setJustNumber(true));
                        }
                        else {
                            variables.add(new Variable(val.stringRepresentation()));
                        }
                    }
                }

                FunctionCall call = new FunctionCall(name, variables);
                program.registerAPIFunction(name);
                return new ElementReturn(call);
            }
            else if (line.equals(1, "'") && line.equals(3, "'")) {
                String character = line.join(1, 3, "");
                Value value = Value.parse(character, program);
                return new Return(value);
            }
        }

        Value value = Value.parse(line.getToken(1), program);

        if (value instanceof nl.hannahsten.pp2lal2pp.lang.Number) {
            return new Return(value);
        }

        return new ElementReturn(new Variable(value.stringRepresentation(), value));
    }

    /**
     * Parses the header comment lines.
     *
     * @return A list of all lines in the header comment WITHOUT #s.
     */
    private List<String> parseHeaderComment(String contents) {
        List<String> header = new ArrayList<>();

        for (String string : contents.split("\n")) {
            String line = string.trim();

            if (!line.startsWith("#")) {
                return header;
            }

            header.add(Regex.replaceAll("^#\\s*;\\s*", line, ""));
        }

        return header;
    }

    /**
     * Parses the line to a FunctionCall.
     *
     * @param line
     *         The line to parse.
     * @return The parsed FunctionCall object.
     */
    private FunctionCall parseFunctionCall(Tokeniser line) {
        try {
            List<Variable> args = new ArrayList<>();

            if (!line.equals(2, ")")) {
                for (int i = 2; i < line.sizeNoComments(); i += 2) {
                    Value value = Value.parse(line.getToken(i), program);

                    if (value instanceof nl.hannahsten.pp2lal2pp.lang.Number) {
                        args.add(new Variable("number" + i, value).setJustNumber(true));
                    }
                    else {
                        args.add(new Variable(line.getToken(i)));
                    }
                }
            }

            String name = line.getToken(0);
            program.registerAPIFunction(name);
            return new FunctionCall(name, args);
        }
        catch (IndexOutOfBoundsException exception) {
            throw new ParseException("Invalid definition for function call '" + line.getOriginal() +
                    "'.");
        }
    }

    /**
     * Checks if the given line is a function call.
     *
     * @param line
     *         The line to check for.
     * @return <code>true</code> if the line is a function call, <code>false</code> otherwise.
     */
    private boolean isFunctionCall(Tokeniser line) {
        String contents = line.getOriginal();

        if (line.sizeNoComments() < 3) {
            return false;
        }

        if (!line.equals(1, "(")) {
            return false;
        }

        return line.equals(line.sizeNoComments() - 1, ")");
    }

    /**
     * Parses the contents of a block.
     *
     * @param lines
     *         The iterator that iterates over the lines.
     * @param line
     *         The line where the iterator is at currently.
     * @return The parsed contents of the block.
     */
    private Block parseBlock(Iterator<String> lines, Tokeniser line) {
        List<Element> body = new ArrayList<>();

        if (!lines.hasNext()) {
            return new Block(body);
        }

        boolean skipped = false;
        if (line.last().equals("{")) {
            line = new Tokeniser(lines.next());
            skipped = true;
        }

        while (true) {
            if (!lines.hasNext()) {
                break;
            }

            if (!skipped) {
                line = new Tokeniser(lines.next());
            }
            skipped = false;

            if (line.isFirst("}")) {
                break;
            }

            StringBuilder comment = new StringBuilder();
            boolean parsed = false;

            // Variable declarations.
            if (line.isFirst("var")) {
                body.add(parseVariableDeclaration(line));
                parsed = true;
            }
            // Loops
            else if (line.isFirst("loop")) {
                body.add(parseLoop(lines, line));
                parsed = true;
            }
            // If-Else statements.
            else if (line.isFirst("if")) {
                body.add(parseIfElse(lines, line));
                parsed = true;
            }
            // Function calls.
            else if (isFunctionCall(line)) {
                body.add(parseFunctionCall(line));
                parsed = true;
            }
            // Return
            else if (line.isFirst("return")) {
                body.add(parseReturn(line));
                parsed = true;
            }
            // Continue
            else if (line.isFirst("continue")) {
                body.add(new Continue());
                parsed = true;
            }
            // Full line comment.
            else if (line.equals(0, "#")) {
                parsed = true;
            }
            // Inject raw assembly.
            else if (line.isFirst("inject")) {
                body.add(parseInject(lines, line));
                parsed = true;
            }

            // Anything else.
            if (!parsed && !line.isFirstIgnore("else", "}")) {
                Operation op = parseOperation(line.iterator(), line);

                if (op != null) {
                    body.add(op);
                }
            }

            // Comments
            for (String token : line) {
                // Comments
                if (token.equals("#")) {
                    comment.append(" ");
                    continue;
                }

                if (comment.length() > 0) {
                    comment.append(token).append(" ");
                }
            }

            if (comment.length() > 0) {
                body.add(new Comment(comment.toString().trim()));
            }

            if (line.sizeNoComments() > 0) {
                if (line.equals(line.sizeNoComments() - 1, "}")) {
                    break;
                }
            }
        }

        return new Block(body);
    }

    /**
     * Parses the operation that is present on the given line.
     *
     * @param lineIterator
     *         The iterator of the line to continue from.
     * @param line
     *         The line containing the expression.
     * @return The parsed operation.
     */
    private Operation parseOperation(Iterator<String> lineIterator, Tokeniser line) {
        Element leftOperandResult;
        ArrayAccess leftOperandArrayAccess = null;
        Operator operatorResult;
        Element rightOperandResult;
        ListIterator<String> listIterator = (ListIterator<String>)lineIterator;

        // isInputOn API function shizz.
        if (line.isFirst("!") && line.equals(1, "isInputOn")) {
            String name = "isInputOn";
            Value value = Value.parse(line.getToken(3), program);
            FunctionCall call = new FunctionCall(name, new ArrayList<Variable>() {{
                add(new Variable("num", value).setJustNumber(true));
            }});
            program.registerAPIFunction(name);

            return new Operation(call, Operator.BOOLEAN_NEGATION, null);
        }

        // Check for assignments
        String original = line.getOriginal();
        Tokeniser originalTokens = new Tokeniser(original);

        // When there are more than 3 tokens, this is a complex assignment (e.g. a = b + 4).
        // When there is an array assignment, there may be more tokens before the `=`-sign.
        int baseIndex = arrayAssignmentSize(originalTokens);
        if (originalTokens.sizeNoComments() > 3 + baseIndex) {
            // Complex left assignment with regular = and :=
            if (originalTokens.equals(1 + baseIndex, "=") || originalTokens.equals(originalTokens.sizeNoComments() - 2, ":=")) {
                // Array assignment.
                if (baseIndex > 0) {
                    leftOperandResult = program.getGlobalArray(originalTokens.getToken(0)).orElseThrow(() ->
                            new ParseException("Global array '" + originalTokens.getToken(0) + "' is undefined.")
                    );
                }
                // Variable assignment.
                else {
                    leftOperandResult = new Variable(originalTokens.getToken(0));
                }

                Tokeniser newLine = new Tokeniser(originalTokens.join(2 + baseIndex, originalTokens.sizeNoComments() - 2 - baseIndex, " "));
                rightOperandResult = parseOperation(newLine.iterator(), newLine);

                if (leftOperandResult instanceof GlobalArray) {
                    ArrayAccess access = parseArrayAccess(originalTokens, 1, true);

                    if (access == null) {
                        return new GlobalArrayAssignment((GlobalArray)leftOperandResult, rightOperandResult);
                    }
                    else {
                        return new GlobalArrayIndexedAssignment((GlobalArray)leftOperandResult, access, rightOperandResult);
                    }
                }
                return new Operation(leftOperandResult, Operator.ASSIGN, rightOperandResult);
            }
            // Complex right assignment with =: (thanks Sten)
            else if (originalTokens.equals(originalTokens.sizeNoComments() - 2, "=:")) {
                Tokeniser newLine = new Tokeniser(originalTokens.join(0, originalTokens.sizeNoComments() - 2, " "));
                rightOperandResult = parseOperation(newLine.iterator(), newLine);
                leftOperandResult = new Variable(originalTokens.getToken(originalTokens.sizeNoComments() - 1));

                return new Operation(leftOperandResult, Operator.ASSIGN, rightOperandResult);
            }
        }

        // Skip over ()
        String currentToken = lineIterator.next();
        if (currentToken.equals(")")) {
            currentToken = lineIterator.next();
        }

        switch (currentToken) {
            case "(":
                leftOperandResult = parseOperation(lineIterator, line);
                break;
            // Unary number negation.
            case "-":
                try {
                    currentToken = lineIterator.next();
                    leftOperandResult = new Number(Integer.parseInt("-" + currentToken));
                }
                catch (NumberFormatException nfe) {
                    return new Operation(new Variable(currentToken), Operator.MULTIPLICATION, Number.MINUS_ONE);
                }
                break;
            // Unary NOT
            case "~":
                try {
                    currentToken = lineIterator.next();
                    leftOperandResult = new Number(~Integer.parseInt(currentToken));
                }
                catch (NumberFormatException nfe) {
                    return new Operation(new Variable(currentToken), Operator.BITWISE_XOR, Number.ALL_1S);
                }

                break;
            default:
                // First token is regular text, hence a variable.
                // When followed by '[', it is a global array.
                Value val = Value.parse(currentToken, program);
                if (val.getObject() instanceof String) {
                    String variableName = (String)val.getObject();
                    String nextToken = Util.peekNext(listIterator);
                    if ("[".equals(nextToken)) {
                        leftOperandResult = program.getGlobalArray(variableName).orElseThrow(() ->
                                new ParseException("Undefined global array '" + variableName + "'")
                        );
                    }
                    else {
                        leftOperandResult = new Variable(currentToken);
                    }
                }
                else {
                    leftOperandResult = val;
                }
                break;
        }

        if (!lineIterator.hasNext()) {
            return null;
        }

        // Skip over ()
        currentToken = lineIterator.next();
        if (currentToken.equals(")")) {
            if (!lineIterator.hasNext()) {
                return null;
            }

            currentToken = lineIterator.next();
        }

        // Operator
        // Function call.
        if (currentToken.equals("(")) {
            String prevToken = line.getToken(listIterator.previousIndex() - 1);
            boolean isApiFunction = APIFunction.isAPIFunction(prevToken);
            boolean isDefinedFunction = program.getFunction(prevToken).isPresent();
            boolean isCurrentFunction = currentFunction.equals(prevToken);

            if ((program.getFunction(prevToken).isPresent() || currentFunction.equals(prevToken)) || isApiFunction) {
                if (isApiFunction) {
                    program.registerAPIFunction(prevToken);
                }

                List<Variable> arguments = parseArguments(lineIterator);
                leftOperandResult = new FunctionCall(prevToken, arguments);
                program.registerAPIFunction(prevToken);

                if (lineIterator.hasNext()) {
                    currentToken = lineIterator.next();
                }
                else {
                    return new Operation(leftOperandResult, null, null);
                }
            }
            else {
                throw new ParseException("Wrong function call at line '" + original +
                        "': isDefined=" + isDefinedFunction +
                        ", isApi=" + isApiFunction +
                        ", isCurrentFunction=" + isCurrentFunction
                );
            }
        }

        // Global array assignment.
        if ("[".equals(currentToken) && lineIterator.hasNext()) {
            // When the next token is ], it means a general array assignment, otherwise it is
            // an indexed assignment.
            currentToken = lineIterator.next();

            // Global variable assignment.
            if ("]".equals(currentToken)) {
                // No index: assign all.
                String globalArrayName = line.getToken(0);
                leftOperandResult = program.getGlobalArray(globalArrayName).orElse(null);
            }
            else {
                // With index: assign only at the index.
                leftOperandArrayAccess = parseArrayAccess(line, listIterator.nextIndex() - 2, true);
                lineIterator.next();
            }

            // Skip over last bracket.
            currentToken = lineIterator.next();
        }

        // Regular operator.
        Optional<Operator> operator = Operator.getBySign(currentToken);
        if (!operator.isPresent()) {
            // Negative number
            if (leftOperandResult instanceof Variable) {
                Variable var = (Variable)leftOperandResult;
                if (var.getName().equals("-")) {
                    leftOperandResult = Value.parse("-" + currentToken, program);
                    return new Operation(leftOperandResult, null, null);
                }
            }

            throw new ParseException("Could not find operator '" + currentToken + "' for line '" + original + "'.");
        }
        else {
            operatorResult = operator.get();
        }

        if (!lineIterator.hasNext()) {
            return new Operation(leftOperandResult, null, null);
        }

        currentToken = lineIterator.next();

        // Second element
        if (currentToken.equals("(")) {
            rightOperandResult = parseOperation(lineIterator, line);
        }
        else if (currentToken.equals("-")) {
            rightOperandResult = Value.parse("-" + lineIterator.next(), program);
        }
        else {
            Value val = Value.parse(currentToken, program);
            if (val.getObject() instanceof String) {
                rightOperandResult = new Variable(currentToken);
            }
            else {
                rightOperandResult = val;
            }
        }

        if (lineIterator.hasNext()) {
            currentToken = lineIterator.next();
        }

        // Second function call.
        if (currentToken.equals("(")) {
            String prevToken = line.getToken(listIterator.previousIndex() - 1);
            if ((program.getFunction(prevToken).isPresent() || currentFunction.equals(prevToken)) || APIFunction.isAPIFunction(prevToken)) {
                List<Variable> arguments = parseArguments(lineIterator);
                rightOperandResult = new FunctionCall(prevToken, arguments);
                program.registerAPIFunction(prevToken);
            }
            else {
                throw new ParseException("Wrong function call at line '" + original + "' (prevToken: " + prevToken + ").");
            }
        }

        // Global array assignment:
        if (leftOperandResult instanceof GlobalArray) {
            GlobalArray globalArray = (GlobalArray)leftOperandResult;

            // - Assign by index.
            if (leftOperandArrayAccess != null) {
                return new GlobalArrayIndexedAssignment(globalArray, leftOperandArrayAccess, rightOperandResult);
            }
            // - Assign all
            else {
                return new GlobalArrayAssignment(globalArray, rightOperandResult);
            }
        }

        return new Operation(leftOperandResult, operatorResult, rightOperandResult);
    }

    /**
     * Counts the total amount of tokens that represent an array index.
     *
     * E.g. {@code []} returns {@code 2}, {@code [34]} returns {@code 3}, {@code 3 + 4}
     * returns {@code 0}.
     */
    private int arrayAssignmentSize(Tokeniser tokens) {
        int count = 0;
        for (int i = 0, size = tokens.size(); i < size; i++) {
            String token = tokens.getToken(i);
            if (count > 0 && "]".equals(token)) {
                count++;
                return count;
            }
            else if ("[".equals(token)) {
                count++;
            }
            // Whenever we have a token after the opening bracket.
            else if (count > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Parses the list of arguments starting at the current line iterator position.
     */
    private List<Variable> parseArguments(Iterator<String> lineIterator) {
        List<Variable> arguments = new ArrayList<>();

        String elt;
        while (lineIterator.hasNext()) {
            elt = lineIterator.next();
            if (")".equals(elt)) {
                break;
            }

            if (",".equals(elt)) {
                continue;
            }

            Value value = Value.parse(elt, program);

            if (value instanceof Number) {
                arguments.add(new Variable(elt, value).setJustNumber(true));
            }
            else {
                arguments.add(new Variable(elt));
            }
        }

        return arguments;
    }

    /**
     * Parses if-else statements.
     *
     * @param lines
     *         The iterator of the LineTokeniser.
     * @param line
     *         The line with the footprint of the if-statement.
     * @return The parsed if-object.
     */
    private IfElse parseIfElse(Iterator<String> lines, Tokeniser line) {
        Block ifBlock;
        Block elseBlock = Block.EMPTY;
        String or = line.getOriginal();

        try {
            if (!line.equals(1, "(") && !line.equals(0, "else")) {
                throw new ParseException("IfElse '" + or + "' has an improper opening bracket");
            }

            // Parse expression.
            String lineNoIfElse = line.getOriginal().replaceAll("(( *else *)?if *\\()|\\) *\\{", "");
            Tokeniser lineExp = new Tokeniser(lineNoIfElse);
            Operation expression = parseOperation(lineExp.iterator(), lineExp);

            // Parse block.
            if (line.equals(line.sizeNoComments() - 1, "{")) {
                ifBlock = parseBlock(lines, line);
            }
            else {
                ifBlock = parseBlock(lines, new Tokeniser(lines.next()));
            }

            // If there is no else.
            if (!line.isFirstIgnore("else", "}")) {
                line = new Tokeniser(lines.next());

                // Ignore comments.
                while (line.isFirst("#")) {
                    line = new Tokeniser(lines.next());
                }
            }

            if (!line.isFirstIgnore("else", "}")) {
                ListIterator<String> listIterator = (ListIterator<String>)lines;
                listIterator.previous();
                return new IfElse(expression, ifBlock, Block.EMPTY);
            }

            // Parse else.
            if (line.isFirstTwoIgnore("else", "if", "}")) {
                Tokeniser newLine = new Tokeniser(line.join(1, line.sizeNoComments() - 1, " "));
                Element elt = parseIfElse(lines, newLine);
                elseBlock = new Block(new ArrayList<Element>() {{
                    add(elt);
                }});
                return new IfElse(expression, ifBlock, elseBlock);
            }

            if (line.isFirstIgnore("else", "}")) {
                if (line.equals(line.sizeNoComments() - 1, "{")) {
                    elseBlock = parseBlock(lines, line);
                }
                else {
                    elseBlock = parseBlock(lines, new Tokeniser(lines.next()));
                }
            }

            return new IfElse(expression, ifBlock, elseBlock);
        }
        catch (IndexOutOfBoundsException exception) {
            throw new ParseException("IfElse is not correctly defined: '" + or + "'.");
        }
    }

    /**
     * Parses loop statements.
     *
     * @param line
     *         The line with the footprint of the loop.
     * @param lines
     *         The iterator of the LineTokeniser.
     * @return The parsed loop-object.
     */
    private Loop parseLoop(Iterator<String> lines, Tokeniser line) {
        Loop loop = null;
        Block block = null;

        try {
            // Check syntax.
            if (!line.equals(1, "(")) {
                throw new ParseException("Loop '" + line.getOriginal() + "' has an improper " +
                        "opening bracket.");
            }

            if (!line.equals(3, "from")) {
                throw new ParseException("Loop '" + line.getOriginal() + "' lacks a from-keyword.");
            }

            if (!line.equals(5, "to")) {
                throw new ParseException("Loop '" + line.getOriginal() + "' lacks a to-keyword.");
            }

            // Parse loop-statement.
            Variable var = new Variable(line.getToken(2));

            nl.hannahsten.pp2lal2pp.lang.Number from;
            try {
                from = new nl.hannahsten.pp2lal2pp.lang.Number(Integer.parseInt(line.getToken(4)));
            }
            catch (NumberFormatException exception) {
                throw new ParseException("Loop '" + line.getOriginal() + "' has an incorrect " +
                        "from-value.");
            }

            nl.hannahsten.pp2lal2pp.lang.Number to;
            try {
                to = new nl.hannahsten.pp2lal2pp.lang.Number(Integer.parseInt(line.getToken(6)));
            }
            catch (NumberFormatException exception) {
                throw new ParseException("Loop '" + line.getOriginal() + "' has an incorrect " +
                        "to-value.");
            }

            if (line.equals(7, ")")) {
                nl.hannahsten.pp2lal2pp.lang.Number step = from.getIntValue() <= to.getIntValue() ? nl.hannahsten.pp2lal2pp.lang.Number.ONE : nl.hannahsten.pp2lal2pp.lang.Number.MINUS_ONE;
                return new Loop(parseBlock(lines, line), var, from, to, step);
            }
            else {
                if (!line.equals(9, ")")) {
                    throw new ParseException("Loop '" + line.getOriginal() + "' has an improper " +
                            "closing bracket.");
                }

                if (!line.equals(7, "step")) {
                    throw new ParseException("Loop '" + line.getOriginal() + "' lacks a " +
                            "step-keyword.");
                }

                Value step = Value.parse(line.getToken(8), program);
                if (step.getObject() instanceof String) {
                    throw new ParseException("Loop '" + line.getOriginal() + "' lacks a correct " +
                            "step-value");
                }

                return new Loop(parseBlock(lines, line), var, from, to, step);
            }
        }
        catch (IndexOutOfBoundsException exception) {
            throw new ParseException("Loop is not correctly defined: '" + line.getOriginal() + "'" +
                    ".");
        }
    }

    /**
     * Turns a line into a variable declaration.
     *
     * @param line
     *         The line where the variable declaration occurs.
     * @return The variable-object representing the declared variable.
     */
    private Declaration parseVariableDeclaration(Tokeniser line) throws ParseException {
        // Simple declaration without specified value.
        if (line.sizeNoComments() == 2) {
            String varName = line.getToken(1);
            return new Declaration(new Variable(varName), Declaration.DeclarationScope.LOCAL);
        }

        if (line.sizeNoComments() < 4 || !line.equals(2, "=")) {
            throw new ParseException("Wrong declaration for variable '" + line.getOriginal() + "'.");
        }

        String variableName = line.getToken(1);

        // Function call
        if (line.equals(4, "(")) {
            String funcName = line.getToken(3);
            List<Variable> variables = new ArrayList<>();

            if (!line.equals(5, ")")) {
                for (int i = 5; i < line.sizeNoComments(); i += 2) {
                    Value val = Value.parse(line.getToken(i), program);

                    if (val instanceof Number) {
                        variables.add(new Variable("num", val).setJustNumber(true));
                    }
                    else {
                        variables.add(new Variable(val.stringRepresentation()));
                    }
                }
            }

            Value defaultValue = new FunctionCall(funcName, variables);
            program.registerAPIFunction(funcName);

            Variable variableToDeclare = new Variable(variableName, defaultValue);
            return new Declaration(variableToDeclare, defaultValue, Declaration.DeclarationScope.LOCAL);
        }

        // First check if it is a variable declaration with an array accessor.
        if (line.equals(4, "[")) {
            ArrayAccess access = parseArrayAccess(line, 4, true);
            if (access == null) {
                throw new ParseException(String.format("Invalid array accessor on line '%s'", line.getOriginal()));
            }

            String arrayName = line.getToken(3);
            GlobalArray array = program.getGlobalArray(arrayName).orElseThrow(() ->
                    new ParseException("Undefined global array '" + arrayName + "'")
            );

            GlobalArrayRead read = new GlobalArrayRead(array, access);
            return new DeclarationFromGlobalArray(new Variable(variableName), read);
        }

        // Regular variable declaration.
        Value defaultValue = Value.parse(line.getToken(3), program);
        Variable variableToDeclare = new Variable(variableName, defaultValue);
        return new Declaration(variableToDeclare, defaultValue, Declaration.DeclarationScope.LOCAL);
    }

    /**
     * Checks the given line for global statements and handles them.
     *
     * @return <code>true</code> if the line was a global statement, <code>false</code> otherwise.
     */
    private boolean parseGlobal(Program program, Tokeniser line) throws ParseException {
        if (!line.isFirst("global")) {
            return false;
        }

        // Global variable array.
        String firstToken = line.getToken(1);
        if ("[".equals(firstToken)) {
            GlobalArray array = parseGlobalArray(line);
            if (array != null) {
                program.addGlobalArray(array);
                return true;
            }
        }

        // Single global variable.
        GlobalVariable var;

        // If there is no value specified
        if (line.sizeNoComments() == 2) {
            var = new GlobalVariable(firstToken, lastComment);
        }
        // It has a default value.
        else {
            var = new GlobalVariable(firstToken, Value.parse(line.getToken(3), program), lastComment);
        }

        program.addGlobalVariable(var);
        return true;
    }

    /**
     * Parses the global array definition that is on this {@code line}.
     * Assumes that this line actually is a global array: throws an exception when there is a
     * problem parsing the global array definition.
     *
     * @return The parsed global array.
     */
    private GlobalArray parseGlobalArray(Tokeniser line) {
        if (line.size() < 5) {
            throw new ParseException(
                    "Invalid global array definition (too few elements): '" + line.getOriginal() + "'"
            );
        }

        ArrayAccess sizeDeclaration = parseArrayAccess(line, 1, true);
        if (sizeDeclaration == null) {
            throw new ParseException("Could not parse array size declaration of '" + line.getOriginal() + "'");
        }

        String arrayName = line.getToken(4);
        Value size = sizeDeclaration.getAccessingIndex();

        // Default value for array.
        Value defaultValue = Number.ZERO;
        if (line.size() >= 7 && "=".equals(line.getToken(5))) {
            defaultValue = Value.parse(line.getToken(6), program);
        }

        // Array size is given by a constant.
        if (size instanceof NumberConstant) {
            String constantName = size.stringRepresentation();
            Optional<Definition> definition = program.getDefinition(constantName);
            if (!definition.isPresent()) {
                throw new ParseException("Could not find constant '" + constantName + "', line: '" + line.getOriginal() + "'");
            }

            int arrayLength = ((Number)definition.get().getValue()).getIntValue();
            GlobalArray array = GlobalArray.withSize(arrayName, arrayLength, lastComment);
            array.setDefaultValue(defaultValue);
            return array;
        }
        // Array size is determined by a given integer.
        else if (size instanceof Number) {
            int arrayLength = ((Number)size).getIntValue();
            GlobalArray array = GlobalArray.withSize(arrayName, arrayLength, lastComment);
            array.setDefaultValue(defaultValue);
            return array;
        }
        // Undefined constant.
        else if (size instanceof Value) {
            throw new ParseException("Undefined constant '" + size.stringRepresentation() + "' on line: '" + line.getOriginal() + "'");
        }

        throw new ParseException("Could not parse global array on line: '" + line.getOriginal() + "'");
    }

    /**
     * Parses an array access.
     *
     * @param line The line that is being parsed.
     * @param startIndex The index of the first access token.
     * @param isGlobal Whether it is a global array size definition (true) or not (false).
     * @return The parsed array access, or `null` when it could not be parsed.
     */
    private ArrayAccess parseArrayAccess(Tokeniser line, int startIndex, boolean isGlobal) {
        if (line.size() - startIndex - 3 < 0) {
            return null;
        }
        if (!"[".equals(line.getToken(startIndex)) || !"]".equals(line.getToken(startIndex + 2))) {
            return null;
        }

        String index = line.getToken(startIndex + 1);
        if (isGlobal) {
            Value value = Value.parse(index, program);

            if (value instanceof Number || value instanceof NumberConstant) {
                return ArrayAccess.newValueIndex(value);
            }
            else {
                // It is a variable when it is not a number.
                Variable variable = new Variable(index);
                return ArrayAccess.newVariableIndex(variable);
            }
        }

        return null;
    }

    /**
     * Parses inject statements.
     *
     * @param line
     *         The line with the footprint of the loop.
     * @param lines
     *         The iterator of the LineTokeniser.
     * @return The parsed inject-object.
     */
    private Inject parseInject(Iterator<String> lines, Tokeniser line) {
        Inject inject;

        try {
            if (!line.equals(1, "{")) {
                throw new ParseException("Incorrect syntax for inject statement: missing opening brace in " + line.getOriginal());
            }

            StringBuilder contents = new StringBuilder();

            while (lines.hasNext()) {
                line = new Tokeniser(lines.next());

                if (line.isFirst("}")) {
                    break;
                }

                if (line.getOriginal().contains("}")) {
                    int untilIndex = line.getOriginal().indexOf("}");
                    contents.append(line.getOriginal().subSequence(0, untilIndex).toString().trim());
                    break;
                }
                else {
                    contents.append(line.getOriginal().trim());
                    contents.append("\n");
                }
            }

            inject = new Inject(contents.toString());
        }
        catch (IndexOutOfBoundsException exception) {
            throw new ParseException("Inject is not correctly defined: '" + line.getOriginal() + "'" +
                                             ".");
        }

        return inject;
    }

}
