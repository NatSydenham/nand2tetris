import java.io.*;
import java.util.List;
import java.util.Scanner;

public class CompilationEngine {


    private String currentToken;
    private File inFile;
    private File outFile;
    private PrintWriter writer;
    private JackTokeniser tokeniser;

    // REGEX

    private final String BINARY_OPERATORS = "^(\\+|-|\\*|/|&|\\||>|<|=)$";
    private final String CLASSVARDEC_REGEX = "^(static|field)$";
    private final String SUBROUTINEDEC_REGEX = "^(constructor|function|method)$";
    private final String TYPE_REGEX = "^(int|char|boolean)$";
    private final String STATEMENT_BEGINNING_REGEX = "^(if|let|while|do|return)$";
    private final String KEYWORD_CONSTANT_REGEX = "^(true|false|null|this)$";
    private final String UNARY_OP_REGEX = "^(-|~)$";

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
        this.outFile = new File(outPath);
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
        this.tokeniser = new JackTokeniser(inPath);
    }

    /**
     * Compiles a complete class. This will only be called once per file,
     * so advance scanner to first line that is not 'tokens'
     */

    public void compileClass() throws Exception {
        advance();
        writer.println("<class>");
        eat("class");

        // class name must be same as file name
        String fileName = inFile.getName().replace(".jack", "");
        eat(fileName.substring(0, 1).toUpperCase() + fileName.substring(1));

        // handle block
        eat("{");
        while (currentToken.matches(CLASSVARDEC_REGEX)) {
            compileClassVarDec();
        }

        while (currentToken.matches(SUBROUTINEDEC_REGEX)) {
            compileSubroutine();
        }
        eat("}");
        writer.println("</class>");
    }

    private void advance() throws Exception {
        do {
            tokeniser.advance();
            currentToken = tokeniser.getCurrentToken();

            if (!tokeniser.hasMoreTokens()){
                return;
            }
        }
        while (tokeniser.tokenType() == Token.WHITESPACE);
    }

    /**
     * Compiles a static declaration or field declaration.
     */

    public void compileClassVarDec() throws Exception {
        writer.println("<classVarDec>");
        // static/field
        eat(currentToken);
        processVarDecs();
        writer.println("</classVarDec>");
    }


    /**
     * compiles a complete method, function,
     * or constructor.
     */

    public void compileSubroutine() throws Exception {
        writer.println("<subroutineDec>");
        eat(currentToken);

        // void or type
        if (currentToken.matches("^void$")) {
            eat(currentToken);
        } else {
            processType();
        }

        // subroutineName
        processIdentifier();
        eat("(");
        compileParameterList();
        eat(")");

        // subroutine body
        writer.println("<subroutineBody>");
        eat("{");

        // optional varDecs
        while (currentToken.equals("var")) {
            compileVarDec();
        }

        // statements
        compileStatements();
        eat("}");
        writer.println("</subroutineBody>");

        writer.println("</subroutineDec>");
    }


    /**
     * compiles a (possibly empty) parameter
     * list, not including the enclosing “()”.
     */

    public void compileParameterList() throws Exception {
        writer.println("<parameterList>");
        while (currentToken.matches(TYPE_REGEX) || tokeniser.tokenType() == Token.IDENTIFIER) {
            processType();
            while (currentToken.equals(",")) {
                eat(",");
                //type
                processType();
                // varName
                processIdentifier();
            }
        }
        writer.println("</parameterList>");
    }

    /**
     * compiles a var declaration.
     */

    public void compileVarDec() throws Exception {
        writer.println("<varDec>");
        eat("var");
        processVarDecs();
        writer.println("</varDec>");
    }

    /**
     * Compiles a sequence of statements, not
     * including the enclosing “{}”.
     */

    public void compileStatements() throws Exception {
        writer.println("<statements>");
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
        writer.println("</statements>");
    }

    /**
     * Compiles a do statement.
     */

    public void compileDo() throws Exception {
        writer.println("<doStatement>");
        eat("do");
        processSubroutineCall();
        eat(";");
        writer.println("</doStatement>");
    }


    /**
     * Compiles a let statement.
     */

    public void compileLet() throws Exception {
        writer.println("<letStatement>");
        eat("let");
        processIdentifier();
        if (currentToken.equals("[")) {
            eat("[");
            compileExpression();
            eat("]");
        }
        eat("=");
        compileExpression();
        eat(";");
        writer.println("</letStatement>");
    }

    /**
     * Compiles a while statement.
     */

    public void compileWhile() throws Exception {
        writer.println("<whileStatement>");
        eat("while");
        processStatementWithConditionAndCodeBlock();
        writer.println("</whileStatement>");
    }


    /**
     * Compiles an if statement, possibly
     * with a trailing else clause.
     */

    public void compileIf() throws Exception {
        writer.println("<ifStatement>");
        eat("if");
        processStatementWithConditionAndCodeBlock();

        // handle optional else clause.

        if (currentToken.equals("else")) {
            eat("else");
            eat("{");
            compileStatements();
            eat("}");
        }

        writer.println("</ifStatement>");
    }

    /**
     * Compiles a return statement.
     */

    public void compileReturn() throws Exception {
        writer.println("<returnStatement>");
        eat("return");
        if (!currentToken.equals(";")) {
            compileExpression();
        }
        eat(";");
        writer.println("</returnStatement>");
    }

    /**
     * Compiles an expression.
     */

    public void compileExpression() throws Exception {
        writer.println("<expression>");
        compileTerm();
        while ((currentToken.matches(BINARY_OPERATORS))) {
            eat(currentToken);
            compileTerm();
        }
        writer.println("</expression>");
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
        writer.println("<term>");

        // Lookahead
        if (tokeniser.tokenType() == Token.IDENTIFIER) {
            eat(currentToken);
            if (currentToken.equals("[")) {
                eat("[");
                compileExpression();
                eat("]");
            } else if (currentToken.equals(".")) {
                eat(".");
                processIdentifier();
                eat("(");
                compileExpressionList();
                eat(")");
            } else if (currentToken.equals("(")){
                eat("(");
                compileExpression();
                eat(")");
            }
        } else if (tokeniser.tokenType() == Token.INT_CONST ||
                tokeniser.tokenType() == Token.STRING_CONST ||
                currentToken.matches(KEYWORD_CONSTANT_REGEX)) {
            eat(currentToken);
        } else if (currentToken.matches(UNARY_OP_REGEX)) {
            eat(currentToken);
            compileTerm();
        } else if (currentToken.equals("(")) {
            eat("(");
            compileExpression();
            eat(")");
        } else {
            throw new Exception("Invalid syntax for term");
        }
        writer.println("</term>");
    }


    /**
     * Compiles a (possibly empty) comma separated list of expressions.
     */

    public void compileExpressionList() throws Exception {
        writer.println("<expressionList>");
        if (!currentToken.equals(")")) {
            compileExpression();
            while (currentToken.equals(",")) {
                eat(",");
                compileExpression();
            }
        }
        writer.println("</expressionList>");
    }

    public void close() {
        writer.flush();
        writer.close();
    }

    private void eat(String token) throws Exception {
        if (!currentToken.equals(token)) {
            throw new Exception("Syntax Error - expected " + token + " when provided token: " + tokeniser.tokenType());
        } else {
            tokenise();
            advance();
        }
    }

    private void processIdentifier() throws Exception {
        if (tokeniser.tokenType() == Token.IDENTIFIER) {
            eat(currentToken);
        } else {
            throw new Exception("Expected <identifier> instead of " + tokeniser.tokenType());
        }
    }

    private void processType() throws Exception {
        if (currentToken.matches(TYPE_REGEX)) {
            eat(currentToken);
            return;
        }
        processIdentifier();
    }

    private void processVarDecs() throws Exception {
        // type
        processType();
        // varName
        processIdentifier();
        while (currentToken.equals(",")) {
            // ,
            eat(currentToken);
            // varName
            processIdentifier();

        }
        eat(";");
    }

    private void processSubroutineCall() throws Exception {
        processIdentifier();
        if (currentToken.equals("(")) {
            eat("(");
            compileExpressionList();
            eat(")");
        } else if (currentToken.equals(".")) {
            eat(".");
            processIdentifier();
            eat("(");
            compileExpressionList();
            eat(")");
        } else {
            throw new Exception("Expected subroutine call");
        }
    }

    private void processStatementWithConditionAndCodeBlock() throws Exception {
        eat("(");
        compileExpression();
        eat(")");
        eat("{");
        compileStatements();
        eat("}");
    }

    private void tokenise() throws Exception {
        Token type = tokeniser.tokenType();
        switch (type) {
            case KEYWORD:
                writer.println("<keyword> " + tokeniser.keyWord() + " </keyword>");
                break;
            case SYMBOL:
                writer.println("<symbol> " + tokeniser.getSym() + " </symbol>");
                break;
            case WHITESPACE:
                break;
            case STRING_CONST:
                writer.println(("<stringConstant> " + tokeniser.stringVal() + " </stringConstant>").replaceAll("\"", ""));
                break;
            case INT_CONST:
                writer.println("<integerConstant> " + tokeniser.intVal() + " </integerConstant>");
                break;
            case IDENTIFIER:
                writer.println("<identifier> " + tokeniser.getIdent() + " </identifier>");
        }
    }
}

