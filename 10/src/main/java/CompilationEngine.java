import java.io.*;
import java.util.Scanner;

public class CompilationEngine {


    private String currentToken;
    private File inFile;
    private File outFile;
    private Scanner scan;
    private PrintWriter writer;

    // REGEX

    private final String BINARY_OPERATORS = "^(\\+|-|\\*|/|&amp;|\\||&gt;|&lt;|=)$";
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
        this.scan = new Scanner(inFile);
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
    }

    /**
     * Compiles a complete class. This will only be called once per file,
     * so advance scanner to first line that is not 'tokens'
     */

    public void compileClass() throws Exception {
        currentToken = scan.nextLine();
        while (currentToken.trim().equals("<tokens>")) {
            currentToken = scan.nextLine();
        }
        writer.println("<class>");
        eat("class");

        // class name must be same as file name
        String fileName = inFile.getName().replace("T.xml", "");
        eat(fileName.substring(0, 1).toUpperCase() + fileName.substring(1));

        // handle block
        eat("{");
        while (processToken().matches(CLASSVARDEC_REGEX)) {
            compileClassVarDec();
        }

        while (processToken().matches(SUBROUTINEDEC_REGEX)) {
            compileSubroutine();
        }
        eat("}");
        writer.println("</class>");
        inFile.delete();
    }

    /**
     * Compiles a static declaration or field declaration.
     */

    public void compileClassVarDec() throws Exception {
        writer.println("<classVarDec>");
        // static/field
        eat(processToken());
        processVarDecs();
        writer.println("</classVarDec>");
    }


    /**
     * compiles a complete method, function,
     * or constructor.
     */

    public void compileSubroutine() throws Exception {
        writer.println("<subroutineDec>");
        eat(processToken());

        // void or type
        if (processToken().matches("^void$")) {
            eat(processToken());
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
        while (processToken().equals("var")) {
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
        while (processToken().matches(TYPE_REGEX) || currentToken.contains("<identifier>")) {
            processType();
            while (processToken().equals(",")) {
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
        while (processToken().matches(STATEMENT_BEGINNING_REGEX)) {
            switch (processToken()) {
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
        if (processToken().equals("[")) {
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

        if (processToken().equals("else")) {
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
        if (!processToken().equals(";")) {
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
        while ((processToken().matches(BINARY_OPERATORS))) {
            eat(processToken());
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
        if (currentToken.contains("<identifier>")) {
            eat(processToken());
            if (processToken().equals("[")) {
                eat("[");
                compileExpression();
                eat("]");
            } else if (processToken().equals(".")) {
                eat(".");
                processIdentifier();
                eat("(");
                compileExpressionList();
                eat(")");
            }
        } else if (currentToken.contains("<integerConstant>") ||
                currentToken.contains("<stringConstant>") ||
                processToken().matches(KEYWORD_CONSTANT_REGEX)) {
            eat(processToken());
        } else if (processToken().matches(UNARY_OP_REGEX)) {
            eat(processToken());
            compileTerm();
        } else if (processToken().equals("(")) {
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
        if (!processToken().equals(")")) {
            compileExpression();
            while (processToken().equals(",")) {
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
        String processedToken = processToken();
        if (!processedToken.equals(token)) {
            throw new Exception("Syntax Error - expected " + token + " when provided token: " + currentToken);
        } else {
            writer.println(currentToken);
            currentToken = scan.nextLine();
        }
    }

    private String processToken() {
        return currentToken.substring(currentToken.indexOf('>') + 1, currentToken.lastIndexOf('<') - 1).trim();
    }

    private void processIdentifier() throws Exception {
        if (currentToken.contains("<identifier>")) {
            eat(processToken());
        } else {
            throw new Exception("Expected <identifier> instead of " + currentToken);
        }
    }

    private void processType() throws Exception {
        if (processToken().matches(TYPE_REGEX)) {
            eat(processToken());
            return;
        }
        processIdentifier();
    }

    private void processVarDecs() throws Exception {
        // type
        processType();
        // varName
        processIdentifier();
        while (processToken().equals(",")) {
            // ,
            eat(processToken());
            // varName
            processIdentifier();

        }
        eat(";");
    }

    private void processSubroutineCall() throws Exception {
        processIdentifier();
        if (processToken().equals("(")) {
            eat("(");
            compileExpressionList();
            eat(")");
        } else if (processToken().equals(".")) {
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

}