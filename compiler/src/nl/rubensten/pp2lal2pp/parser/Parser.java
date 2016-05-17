package nl.rubensten.pp2lal2pp.parser;

import nl.rubensten.pp2lal2pp.ParseException;
import nl.rubensten.pp2lal2pp.lang.*;
import nl.rubensten.pp2lal2pp.lang.Number;

import java.util.*;

/**
 * Takes one big code string and parses it to a {@link Program}.
 * <p>
 * This class does not handle inclusions and definitions.
 *
 * @author Ruben Schellekens
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

            if (line.startsWith("#")) {
                String comment = line.replaceAll("^#;?\\s*", "");
                lastComment = new Comment(comment);
                pp2doc.add(comment);
                continue;
            }

            if (!line.startsWith("function")) {
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

            function = new Function(currentFunction, new ArrayList<>(pp2doc), arguments);
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
                body.add(parseVariable(line));
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
        else {
            return new Return(Value.parse(line.getToken(1)));
        }
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

            header.add(line.replaceAll("^#\\s*;\\s*", ""));
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

            for (int i = 2; i < line.sizeNoComments(); i += 2) {
                Value value = Value.parse(line.getToken(i));
                if (value.getObject() instanceof String) {
                    args.add(new Variable(line.getToken(i)));
                }
                else {
                    args.add(new Variable("number" + i, value).setJustNumber(true));
                }
            }

            String name = line.getToken(0);
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

        while (true) {
            line = new Tokeniser(lines.next());

            if (line.isFirst("}")) {
                break;
            }

            if (line.sizeNoComments() > 0) {
                if (line.last().equals("{")) {
                    continue;
                }
            }

            StringBuilder comment = new StringBuilder();
            boolean parsed = false;

            // Variable declarations.
            if (line.isFirst("var")) {
                body.add(parseVariable(line));
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
     * @param it
     *         The iterator of the line to continue from.
     * @param line
     *         The line containing the expression.
     * @return The parsed operation.
     */
    private Operation parseOperation(Iterator<String> it, Tokeniser line) {
        Element first;
        Operator op = null;
        Element second;
        ListIterator<String> li = (ListIterator<String>)it;

        // Check for assignments
        String or = line.getOriginal();
        Tokeniser orTokens = new Tokeniser(or);
        if (orTokens.sizeNoComments() > 3) {
            if (orTokens.equals(1, "=") || orTokens.equals(orTokens.sizeNoComments() - 2, ":=")) {
                first = new Variable(orTokens.getToken(0));
                op = Operator.ASSIGN;

                Tokeniser newLine = new Tokeniser(orTokens.join(2, orTokens.sizeNoComments() - 2, " "));
                second = parseOperation(newLine.iterator(), newLine);

                return new Operation(first, op, second);
            }
            else if (orTokens.equals(orTokens.sizeNoComments() - 2, "=:")) {
                Tokeniser newLine = new Tokeniser(orTokens.join(0, orTokens.sizeNoComments() - 2, " "));
                second = parseOperation(newLine.iterator(), newLine);

                op = Operator.ASSIGN;
                first = new Variable(orTokens.getToken(orTokens.sizeNoComments() - 1));

                return new Operation(first, op, second);
            }
        }

        String token = it.next();
        if (token.equals(")")) {
            token = it.next();
        }

        // First element
        if (token.equals("(")) {
            first = parseOperation(it, line);
        }
        else {
            Value val = Value.parse(token);
            if (val.getObject() instanceof String) {
                if (val.getObject() instanceof String) {
                    first = new Variable(token);
                }
                else {
                    first = new Variable(token);
                }
            }
            else {
                first = val;
            }
        }

        if (!it.hasNext()) {
            return null;
        }

        token = it.next();
        if (token.equals(")")) {
            if (!it.hasNext()) {
                return null;
            }

            token = it.next();
        }

        // Operator
        // Function call.
        if (token.equals("(")) {
            String prevToken = line.getToken(li.previousIndex() - 1);
            if ((program.getFunction(prevToken).isPresent() || currentFunction.equals(prevToken))) {
                List<Variable> arguments = new ArrayList<>();

                String elt;
                while (!(elt = it.next()).equals(")")) {
                    if (elt.equals(",")) {
                        continue;
                    }

                    arguments.add(new Variable(elt));
                }

                first = new FunctionCall(prevToken, arguments);
                token = it.next();
            }
            else {
                throw new ParseException("Wrong function call at line '" + or + "'.");
            }
        }
        // Regular operator.
        Optional<Operator> operator = Operator.getBySign(token);
        if (!operator.isPresent()) {
            throw new ParseException("Could not find operator '" + token + "' for line '" + or +
                    "'.");
        }
        else {
            op = operator.get();
        }

        if (!it.hasNext()) {
            return new Operation(first, null, null);
        }

        token = it.next();

        // Second element
        if (token.equals("(")) {
            second = parseOperation(it, line);
        }
        else {
            Value val = Value.parse(token);
            if (val.getObject() instanceof String) {
                second = new Variable(token);
            }
            else {
                second = val;
            }
        }

        if (it.hasNext()) {
            token = it.next();
        }

        // Second function call.
        if (token.equals("(")) {
            String prevToken = line.getToken(li.previousIndex() - 1);
            if ((program.getFunction(prevToken).isPresent() || currentFunction.equals(prevToken))) {
                List<Variable> arguments = new ArrayList<>();

                String elt;
                while (!(elt = it.next()).equals(")")) {
                    if (elt.equals(",")) {
                        continue;
                    }

                    arguments.add(new Variable(elt));
                }

                second = new FunctionCall(prevToken, arguments);
            }
            else {
                throw new ParseException("Wrong function call at line '" + or + "'.");
            }
        }

        return new Operation(first, op, second);
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

            Number from;
            try {
                from = new Number(Integer.parseInt(line.getToken(4)));
            }
            catch (NumberFormatException exception) {
                throw new ParseException("Loop '" + line.getOriginal() + "' has an incorrect " +
                        "from-value.");
            }

            Number to;
            try {
                to = new Number(Integer.parseInt(line.getToken(6)));
            }
            catch (NumberFormatException exception) {
                throw new ParseException("Loop '" + line.getOriginal() + "' has an incorrect " +
                        "to-value.");
            }

            if (line.equals(7, ")")) {
                Number step = from.getIntValue() <= to.getIntValue() ? Number.ONE : Number.MINUS_ONE;
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

                Value step = Value.parse(line.getToken(8));
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
     * Turns a line into a variable.
     *
     * @param line
     *         The line where the variable declaration occurs.
     * @return The variable-object representing the declared variable.
     */
    private Declaration parseVariable(Tokeniser line) throws ParseException {
        if (line.sizeNoComments() == 2) {
            String varName = line.getToken(1);
            return new Declaration(new Variable(varName), Declaration.DeclarationScope.LOCAL);
        }
        else if (line.sizeNoComments() >= 4) {
            if (!line.equals(2, "=")) {
                throw new ParseException("Wrong declaration for variable '" + line.getOriginal() +
                        "'.");
            }

            String varName = line.getToken(1);
            Value value = Value.parse(line.join(3, line.sizeNoComments() - 3, ""));
            return new Declaration(new Variable(varName, value), value, Declaration.DeclarationScope
                    .LOCAL);
        }
        else {
            throw new ParseException("Wrong declaration for variable '" + line.getOriginal() + "'.");
        }
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

        String firstToken = line.getToken(1);
        GlobalVariable var;

        // If there is no value specified
        if (line.sizeNoComments() == 2) {
            var = new GlobalVariable(firstToken, lastComment);
        }
        else {
            var = new GlobalVariable(firstToken, Value.parse(line.getToken(3)), lastComment);
        }

        program.addGlobalVariable(var);
        return true;
    }

}
