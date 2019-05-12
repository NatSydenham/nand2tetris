import java.io.*;
import java.util.List;
import java.util.Scanner;

public class CompilationEngine {


    private String currentToken;
    private File inFile;
    private JackTokeniser tokeniser;
    private SymbolTable table;
    private VMWriter writer;
    private String currentClass;
    private int labelCounter = 0;

    // REGEX

    private static final String BINARY_OPERATORS = "^(\\+|-|\\*|/|&|\\||>|<|=)$";
    private static final String CLASSVARDEC_REGEX = "^(static|field)$";
    private static final String SUBROUTINEDEC_REGEX = "^(constructor|function|method)$";
    private static final String TYPE_REGEX = "^(int|char|boolean)$";
    private static final String STATEMENT_BEGINNING_REGEX = "^(if|let|while|do|return)$";

    // METHODS

    /**
     * creates a new compilation engine with
     * the given input and output. The next
     * method called must be
     * compileClass().
     *
     * @param inPath  the path to the input file.
     * @param outPath the path to the output file.
     * @throws IOException if input or output file cannot be found.
     */

    public CompilationEngine(String inPath, String outPath) throws IOException {
        this.inFile = new File(inPath);
        this.tokeniser = new JackTokeniser(inPath);
        this.table = new SymbolTable();
        this.writer = new VMWriter(outPath);
        this.labelCounter = 0;
    }

    /**
     * Compiles a complete class. This will only be called once per file,
     * so advance scanner to first line that is not 'tokens'
     */

    public void compileClass() throws Exception {
        advance();
        eat("class");

        // class name must be same as file name
        String fileName = inFile.getName().replace(".jack", "");
        String className = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
        eat(className);
        currentClass = className;

        // handle block
        eat("{");
        while (currentToken.matches(CLASSVARDEC_REGEX)) {
            compileClassVarDec();
        }

        while (currentToken.matches(SUBROUTINEDEC_REGEX)) {
            compileSubroutine();
        }
        eat("}");
    }

    private void advance() throws Exception {
        do {
            tokeniser.advance();
            currentToken = tokeniser.getCurrentToken();
            if (!tokeniser.hasMoreTokens()) {
                return;
            }
        }
        while (tokeniser.tokenType() == Token.WHITESPACE);
        System.out.println(currentToken);
    }

    /**
     * Compiles a static declaration or field declaration.
     */

    public void compileClassVarDec() throws Exception {

        // We do not enter this method unless currentToken is static or field, so record kind and advance over the token.
        Kind kind = Kind.stringToKind(currentToken);
        advance();


        // get type of current token & advance over.
        if (!currentToken.matches(TYPE_REGEX) && tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected a type or an identifier instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        String type = currentToken;
        advance();


        // get name of var declaration & create entry in symbol table, then advance to next token.
        if (tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected var name (identifier) instead of " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        table.define(currentToken, type, kind);
        advance();


        // create entry in symbol table for every subsequent declaration on this line.
        while (currentToken.equals(",")) {
            // advance over comma
            advance();

            // expect varName
            if (tokeniser.tokenType() != Token.IDENTIFIER) {
                throw new Exception("Expected var name (identifier) instead of " + currentToken + ", which is of type: " + tokeniser.tokenType());
            }
            table.define(currentToken, type, kind);
            advance();
        }
        eat(";");
    }

    /**
     * compiles a complete method, function,
     * or constructor.
     */

    public void compileSubroutine() throws Exception {
        // We do not enter this method unless the current token is constructor, function, or method, so no error checking
        String subroutineType = currentToken;
        advance();

        // As we are starting a new subroutine scope, we can clear the subroutine scope of the current symbol table.
        table.startSubroutine();
        // record whether return type is void or a type
        if (!currentToken.equals("void") && !currentToken.matches(TYPE_REGEX) && tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected 'void' or type, instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        String returnType = currentToken;
        advance();

        // record subroutineName
        if (tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected subroutineName (identifier) instead of " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        String subroutineName = currentToken;
        advance();

        // Before compiling the optional parameter list, we need to define 'this' as the first argument in the symboltable if the subroutine is a method.
        if (subroutineName.equals("method")) {
            table.define("this", currentClass, Kind.ARG);
        }

        // record optional parameter list.
        eat("(");
        compileParameterList();
        eat(")");

        // compile the subroutine's body - this algorithm varies based on whether it is a constructor, function, or method.
        eat("{");
        // compile optional varDecs
        while (currentToken.equals("var")) {
            compileVarDec();
        }

        // compile function definition
        writer.writeFunction(currentClass + "." + subroutineName, table.varCount(Kind.VAR));

        // If the subroutine is a method or a constructor, load 'this' pointer.
        if (subroutineType.equals("method")) {
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        } else if (subroutineType.equals("constructor")) {
            writer.writePush("constant", table.varCount(Kind.FIELD));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("pointer", 0);
        }

        // compile statements until token is }, indicating end of method body.

        while (!currentToken.equals("}")) {
            compileStatement();
        }
        advance();
    }

    /**
     * compiles a (possibly empty) parameter
     * list, not including the enclosing “()”.
     */

    public void compileParameterList() throws Exception {

        // Parameter list may be optional, so check that we have a arg type.
        if (currentToken.matches(TYPE_REGEX) || tokeniser.tokenType() == Token.IDENTIFIER) {
            String argType = currentToken;
            advance();

            // Get the varName, and define in local symbolTable
            if (tokeniser.tokenType() != Token.IDENTIFIER) {
                throw new Exception("Expected a varName (identifier) instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
            }
            table.define(currentToken, argType, Kind.ARG);
            advance();

            // Define all other parameters in the symbolTable. Parameters are comma separated.
            while (currentToken.equals(",")) {
                advance();

                // Get type
                if (!currentToken.matches(TYPE_REGEX) && tokeniser.tokenType() != Token.IDENTIFIER) {
                    throw new Exception("Expected type, instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
                }
                argType = currentToken;
                advance();

                // use the current token to define the parameter in the current scope's symbol table.
                if (tokeniser.tokenType() != Token.IDENTIFIER) {
                    throw new Exception("Expected a varName (identifier) instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
                }
                table.define(currentToken, argType, Kind.ARG);
                advance();
            }
        }
    }

    /**
     * compiles a var declaration.
     */

    public void compileVarDec() throws Exception {
        // We only enter this method if the currentToken is 'var', so advance over current Token.
        advance();

        // Get type
        if (!currentToken.matches(TYPE_REGEX) && tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected type, instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        String varType = currentToken;
        advance();


        // Get the varName, and define in local symbolTable
        if (tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected a varName (identifier) instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        table.define(currentToken, varType, Kind.VAR);
        advance();

        // Can declare multiple vars of same type on same line if comma separated.

        while (currentToken.equals(",")) {
            // advance over the comma
            advance();

            // Get the varName, and define in local symbolTable
            if (tokeniser.tokenType() != Token.IDENTIFIER) {
                throw new Exception("Expected a varName (identifier) instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
            }
            table.define(currentToken, varType, Kind.VAR);
            advance();
        }

        // Requirement for variable declaration to end in a semicolon.
        eat(";");
    }

    /**
     * Compiles a sequence of statements, not
     * including the enclosing “{}”.
     */

    public void compileStatement() throws Exception {
        while (currentToken.matches(STATEMENT_BEGINNING_REGEX)) {
            switch (currentToken) {
                case "if":
                    compileIf();
                    break;
                case "let":
                    compileLet();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
            }
        }
    }

    /**
     * Compiles a do statement.
     */

    public void compileDo() throws Exception {
        // Advance over 'do'
        advance();

        // Subroutine call

        int numArgs = 0;

        // Subroutine name
        if (tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected a subroutine name or object name (identifier) instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }

        // Here, name can be either the name of a subroutine, or it can be an object.
        String functionName = currentToken;
        advance();

        if (currentToken.equals("(")) {
            String subroutineName = functionName;
            String name = currentClass + "." + subroutineName;
            numArgs++;
            writer.writePush("pointer", 0);
            eat("(");
            numArgs += compileExpressionList();
            eat(")");
            writer.writeCall(name, numArgs);

        } else if (currentToken.equals(".")) {
            advance();
            String subroutineName = currentToken;
            String type = table.typeOf(functionName);
            if (!type.equals("none")) {
                Kind kind = table.kindOf(functionName);
                int index = table.indexOf(functionName);
                writer.writePush(Kind.kindToSegment(kind), index - 1);
                numArgs++;
                String name = type + "." + subroutineName;
                advance();
                eat("(");
                numArgs += compileExpressionList();
                eat(")");
                writer.writeCall(name, numArgs);
            } else {
                String name = functionName + "." + subroutineName;
                advance();
                eat("(");
                numArgs += compileExpressionList();
                eat(")");
                writer.writeCall(name, numArgs);
            }
        } else {
            throw new Exception("Expected subroutine call");
        }

        eat(";");
        writer.writePop("temp", 0);
    }


    /**
     * Compiles a let statement.
     */

    public void compileLet() throws Exception {
        eat("let");

        // Get the varName
        if (tokeniser.tokenType() != Token.IDENTIFIER) {
            throw new Exception("Expected a varName (identifier) instead of: " + currentToken + ", which is of type: " + tokeniser.tokenType());
        }
        String varName = currentToken;
        advance();

        // If there is an array declaration
        boolean arrDec = false;
        if (currentToken.equals("[")) {
            arrDec = true;
            advance();
            writer.writePush(Kind.kindToSegment(table.kindOf(varName)), table.indexOf(varName) - 1);
            compileExpression();
            eat("]");
            writer.writeArithmetic("add");
        }

        eat("=");
        compileExpression();
        eat(";");

        // If there was an array declaration, generate array access code, otherwise directly pop the value from the stack:
        if (arrDec) {
            writer.writePop("temp", 0);
            writer.writePop("pointer", 1);
            writer.writePush("temp", 0);
            writer.writePop("that", 0);
        } else {
            writer.writePop(Kind.kindToSegment(table.kindOf(varName)), table.indexOf(varName) - 1);
        }
    }

    /**
     * Compiles a while statement.
     */

    public void compileWhile() throws Exception {
        // Declare labels
        String labelEnd = "WHILE_END" + labelCounter;
        String labelLoop = "WHILE" + labelCounter;
        labelCounter++;

        // advance over while, as this will only be called if token is while.
        advance();

        writer.writeLabel(labelLoop);
        // Condition
        eat("(");
        compileExpression();
        writer.writeArithmetic("not");
        eat(")");
        eat("{");
        writer.writeIf(labelEnd);

        while (!currentToken.equals("}")) {
            compileStatement();
        }
        // Always loop back to top and re-evaluate condition.
        writer.writeGoto(labelLoop);
        // Broken out of the loop.
        writer.writeLabel(labelEnd);
        eat("}");
    }


    /**
     * Compiles an if statement, possibly
     * with a trailing else clause.
     */

    public void compileIf() throws Exception {
        // Declare labels
        String labelFalse = "IF_FALSE" + labelCounter;
        String labelTrue = "IF_TRUE" + labelCounter;
        String labelEnd = "IF_END" + labelCounter;
        labelCounter++;

        eat("if");
        eat("(");
        compileExpression();
        eat(")");
        eat("{");

        writer.writeIf(labelTrue);
        writer.writeGoto(labelFalse);
        writer.writeLabel(labelTrue);
        while (!currentToken.equals("}")) {
            compileStatement();
        }
        writer.writeGoto(labelEnd);
        eat("}");
        writer.writeLabel(labelFalse);

        if (currentToken.equals("else")) {
            eat("else");
            eat("{");

            while (!currentToken.equals("}")) {
                compileStatement();
            }
            eat("}");
        }
        writer.writeLabel(labelEnd);
    }

    /**
     * Compiles a return statement.
     */

    public void compileReturn() throws Exception {
        // Advance over 'return'
        advance();

        // Check for an expression. If there is no expression, push 0 to the stack.
        if (currentToken.equals(";")) {
            writer.writePush("constant", 0);
        } else {
            compileExpression();
        }
        eat(";");
        writer.writeReturn();
    }

    /**
     * Compiles an expression - form is term (op term)*
     */

    public void compileExpression() throws Exception {
        compileTerm();
        while ((currentToken.matches(BINARY_OPERATORS))) {
            switch (currentToken) {
                case "+":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("add");
                    break;
                case "*":
                    advance();
                    compileTerm();
                    writer.writeCall("Math.multiply", 2);
                    break;
                case "/":
                    advance();
                    compileTerm();
                    writer.writeCall("Math.divide", 2);
                    break;
                case "-":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("sub");
                    break;
                case "<":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("lt");
                    break;
                case ">":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("gt");
                    break;
                case "=":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("eq");
                    break;
                case "&":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("and");
                    break;
                case "|":
                    advance();
                    compileTerm();
                    writer.writeArithmetic("or");
                    break;
                default:
                    throw new Exception("Operator: " + currentToken + " is not a binary operator");
            }
        }
    }

    /**
     * Compiles a term. This method is faced
     * with a slight difficulty when trying to
     * decide between some of the alternative
     * rules. Specifically, if the current token
     * is an identifier, it must still distinguish
     * between a variable, an array entry, and
     * a subroutine call. The distinction can be
     * made by looking ahead one extra token.
     * A single look-ahead token, which may
     * be one of “[“, “(“, “.”, suffices to
     * distinguish between the three
     * possibilities. Any other token is not
     * part of this term and should not be
     * advanced over.
     */

    public void compileTerm() throws Exception {
        // Lookahead
        if (tokeniser.tokenType() == Token.IDENTIFIER) {
            String temp = currentToken;
            advance();
            if (currentToken.equals("[")) {
                // In the case of an array, we need to push the base address and the array variable on to the stack.
                writer.writePush(Kind.kindToSegment(table.kindOf(temp)), table.indexOf(temp) - 1);
                eat("[");
                compileExpression();
                eat("]");
                // Add the base address, pop into 'that' pointer, then push the pointer to stack.
                writer.writeArithmetic("add");
                writer.writePop("pointer", 1);
                writer.writePush("that", 0);

            } else if (currentToken.equals(".") || currentToken.equals("(")) {
                int numArgs = 0;
                // Here, name can be either the name of a subroutine, or it can be an object.
                String functionName = temp;
                if (currentToken.equals("(")) {
                    String subroutineName = functionName;
                    String name = currentClass + "." + subroutineName;
                    numArgs++;
                    writer.writePush("pointer", 0);
                    eat("(");
                    numArgs += compileExpressionList();
                    eat(")");
                    writer.writeCall(name, numArgs);
                } else {
                    advance();
                    String subroutineName = currentToken;
                    String type = table.typeOf(functionName);

                    if (!type.equals("none")) {
                        Kind kind = table.kindOf(functionName);
                        int index = table.indexOf(functionName);
                        writer.writePush(Kind.kindToSegment(kind), index - 1);
                        numArgs++;
                        String name = type + "." + subroutineName;
                        advance();
                        eat("(");
                        numArgs += compileExpressionList();
                        eat(")");
                        writer.writeCall(name, numArgs);
                    } else {
                        String name = functionName + "." + subroutineName;
                        advance();
                        eat("(");
                        numArgs += compileExpressionList();
                        eat(")");
                        writer.writeCall(name, numArgs);
                    }
                }
            } else {
                // It's a var name so push it to the stack.
                writer.writePush(Kind.kindToSegment(table.kindOf(temp)), table.indexOf(temp) - 1);
            }
        } else if (tokeniser.tokenType() == Token.INT_CONST) {
            writer.writePush("constant", tokeniser.intVal());
            advance();
        } else if (tokeniser.tokenType() == Token.STRING_CONST) {
            // Build the each character.
            String stringToPush = tokeniser.stringVal();
            System.out.println("Pushing " + stringToPush);
            writer.writePush("constant", stringToPush.length());
            writer.writeCall("String.new", 1);
            for (int i = 0; i < stringToPush.length(); i++) {
                if (stringToPush.charAt(i) == '"'){
                    continue;
                }
                writer.writePush("constant", (int) stringToPush.charAt(i));
                writer.writeCall("String.appendChar", 2);
            }
            advance();
        } else if (tokeniser.tokenType() == Token.KEYWORD && tokeniser.keyWord().equals("true")) {
            writer.writePush("constant", 0);
            writer.writeArithmetic("not");
            advance();
        } else if (tokeniser.tokenType() == Token.KEYWORD && (tokeniser.keyWord().equals("false") || tokeniser.keyWord().equals("null"))) {
            writer.writePush("constant", 0);
            advance();
        } else if (tokeniser.tokenType() == Token.KEYWORD && tokeniser.keyWord().equals("this")) {
            writer.writePush("pointer", 0);
            advance();
        } else if (tokeniser.tokenType() == Token.SYMBOL && tokeniser.getSym().equals("(")) {
            advance();
            compileExpression();
            eat(")");
        } else if (tokeniser.tokenType() == Token.SYMBOL && (tokeniser.getSym().equals("-") || tokeniser.getSym().equals("~"))) {
            String symbol = currentToken;
            advance();
            compileTerm();
            if (symbol.equals("-")) {
                writer.writeArithmetic("neg");
            } else {
                writer.writeArithmetic("not");
            }
        } else {
            throw new Exception(currentToken + " Not valid syntax for term");
        }
    }


    /**
     * Compiles a (possibly empty) comma separated list of expressions.
     *
     * @return the number of arguments.
     */

    public int compileExpressionList() throws Exception {
        int numArgs = 0;
        // If there are no expressions, return 0 arguments.
        if (currentToken.equals(")")) {
            return numArgs;
        } else {
            numArgs++;
            compileExpression();
            while (currentToken.equals(",")) {
                advance();
                compileExpression();
                numArgs++;
            }
        }
        return numArgs;
    }

    public void close() {
        writer.close();
    }

    private void eat(String token) throws Exception {
        if (!currentToken.equals(token)) {
            throw new Exception("Syntax Error - expected " + token + " when provided token: " + currentToken);
        } else {
            advance();
        }
    }
}