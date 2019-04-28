import java.io.*;
import java.util.Arrays;

public class JackTokeniser {

    private StringBuilder currentToken;
    private StringBuilder input;
    private int len;
    private int pos;
    private final String KEYWORD_REGEX = "^(class|method|function|constructor|int|boolean|char|void|var|static|field|let|do|if|else|while|return|true|false|null|this)$";
    private final String SYMBOLS = "[{}()\\[\\].,;+\\-*/&|<>=~]";
    private final String IDENTIFIER_REGEX = "^[A-Za-z_][A-Za-z0-9_]*$";
    private final String STR_CONST_REGEX = "^\"[^\"\\n]*\"$";
    private final String[] KEYWORDS = {"class", "method", "function", "constructor", "int", "boolean", "char", "void", "var",
            "static", "field", "let", "do", "if", "else", "while", "return", "true", "false", "null", "this"};

    /**
     * The tokenizer removes all comments and white space from the input stream
     * and breaks it into Jack language tokens, as specified in the Jack grammar.
     *
     * @throws FileNotFoundException if the input file cannot be found.
     */


    public JackTokeniser(String filePath) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(filePath));
        this.input = new StringBuilder();
        String line;
        while ((line = file.readLine()) != null) {
            input.append(line.replaceAll("(//|/\\*\\*|^\\s*\\*\\s*).*", ""));

        }
        this.len = input.length();
        this.pos = 0;
    }

    /**
     * Checks to see if there are more tokens in the input.
     *
     * @return false if there are no more tokens in the input file, otherwise true.
     */
    public boolean hasMoreTokens() {
        if (pos < len) {
            return true;
        }
        return false;
    }

    /**
     * Advances to the next token. Should only be called if hasMoreTokens returns true.
     */

    public void advance() {
        currentToken = new StringBuilder();

        while (pos < len) {
            if (Character.toString(input.charAt(pos)).matches(SYMBOLS)) {
                // If we have some input already, we do not want to include the symbol as part of the token.
                if (currentToken.length() > 0) {
                    return;
                }
                // If we do not, this is the current token, so append, advance the pointer, and return.
                else {
                    currentToken.append(input.charAt(pos));
                    pos++;
                    return;
                }
            }
            // Else, if we encounter whitespace not part of a string, advance and return.
            // NOTE: Analyser will handle case with multiple spaces in a row. Simply do not include in token.

            else if (Character.toString(input.charAt(pos)).matches("\\s") && !currentToken.toString().contains("\"")) {
                pos++;
                return;
            }
            // Else, we have a character we can just append to the current token, and advance the pointer.
            else {
                currentToken.append(input.charAt(pos));
                pos++;
            }
        }
    }

    /**
     * Provides the type of the current token.
     *
     * @return the type of the current token
     */

    public Token tokenType() throws Exception {

        // Handle case where file has multiple whitespace characters in a row, avoiding NPE
        if (currentToken.length() == 0) {
            return Token.WHITESPACE;
        }

        String token = currentToken.toString();
        if (token.matches(KEYWORD_REGEX)) {
            return Token.KEYWORD;
        } else if (token.matches(SYMBOLS)) {
            return Token.SYMBOL;
        } else if (token.matches(IDENTIFIER_REGEX)) {
            return Token.IDENTIFIER;
        } else if (token.matches(STR_CONST_REGEX)) {
            return Token.STRING_CONST;
        }
        // if method has not returned, it may be an integer constant. Attempt to parse as int, and handle exceptions.
        // NOTE: 0 <= int_const <= 32767

        try {
            int potentialInt = Integer.parseInt(token);
            if (potentialInt >= 0 && potentialInt <= 32767) {
                return Token.INT_CONST;
            } else {
                throw new Exception("Integer " + token + " must be 0 <= num <= 32767");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Token " + token + " Not a valid token...");
        }
    }

    /**
     * If the type of the current token is keyword, returns which keyword
     *
     * @return which keyword the current token is.
     * @throws if current token is not a keyword.
     */

    public String keyWord() throws Exception {
        String wordString = currentToken.toString();
        if (Arrays.asList(KEYWORDS).contains(wordString)) {
            return wordString;
        } else {
            throw new Exception("Keyword: " + wordString + " not valid keyword");
        }
    }

    /**
     * Provides the current symbol. Should only be called if current token is Symbol.
     *
     * @return a character which represents the current symbol.
     */
    public String getSym() {

        String symString = currentToken.toString();
        switch (symString) {
            case "<":
                return "&lt;";
            case "\"":
                return "&quot;";
            case ">":
                return "&gt;";
            case "&":
                return "&amp;";
            default:
                return symString;
        }
    }

    /**
     * Provides the current identifier. Should only be called if current token is Identifier.
     *
     * @return the identifier which is the current token.
     */

    public String getIdent() {
        return currentToken.toString();
    }

    /**
     * Provides the integer value of the current token. Should only be called when current token is int_const
     *
     * @return the integer value which is the current token.
     * @throws NumberFormatException if currentToken is not an int_const
     */
    public int intVal() {
        return Integer.parseInt(currentToken.toString());
    }

    /**
     * Provides the string value of the current token. Should only be called when current token is string_const.
     */

    public String stringVal() {
        return currentToken.toString();
    }
}
