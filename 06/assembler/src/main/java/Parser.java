
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


/**
 * Parser: Encapsulates access to the input code. Reads an assembly language
 * command, parses it, and provides convenient access to the command’s
 * components (fields and symbols). In addition, removes all white space and
 * comments.
 */

public class Parser {

    private static final String BLANK_LINE_REGEX = "^\\s*$";
    private static final String COMMENT_LINE_REGEX = "^\\s*\\/\\/.*$";
    private static final String A_INSTRUCTION_REGEX = "\\s*@.+\\s*(\\/\\/.*)*";
    private static final String L_INSTRUCTION_REGEX = "\\s*\\(.+\\)\\s*(\\/\\/.*)*";
    private static final String C_INSTRUCTION_REGEX = "\\s*(A|M|D|AM|AD|MD|AMD)?\\s*=?\\s*(0|1|-1|D|A|!D|!A|-D|-A|D\\+1|A\\+1|D-1|A-1|D\\+A|D-A|A-D|D&A|D\\|A|M|!M|-M|M\\+1|M-1|D\\+M|D-M|M-D|D&M|D\\|M)?\\s*(;?\\s*(JGT|JEQ|JGE|JLT|JNE|JLE|JMP)?)\\s*(//.*)*";
    String currentCommand;
    private File asmFile;
    private Scanner fileScan;
    private int lineNumber;

    /**
     * Opens the .asm file and prepares to parse it.
     *
     * @param filePath the path to the .asm file.
     *      */
    public Parser(String filePath) {
        this.asmFile = new File(filePath);
        this.lineNumber = 0;
    }

    public void createNewScanner() throws FileNotFoundException {
        this.fileScan = new Scanner(asmFile);
    }

    /**
     * Resets the line number for the parser - use after first pass.
     */
    void resetLineNumber() {
        this.lineNumber = 0;
    }

    /**
     * @return whether there are more commands in the input file.
     */
    boolean hasMoreCommands() {
        boolean result = fileScan.hasNextLine() ? true : false;
        return result;
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * Should be called only if hasMoreCommands() is true. Initially there is no
     * current command.
     */
    void advance() {
        this.currentCommand = fileScan.nextLine().trim();
        this.lineNumber++;
    }

    /**
     * Returns the type of the current command: m A_COMMAND for @Xxx where Xxx is
     * either a symbol or a decimal number m C_COMMAND for dest=comp;jump m
     * L_COMMAND (actually, pseudocommand) for (Xxx) where Xxx is a symbol
     *
     * @return The type of the current command.
     * @throws Exception if invalid command.
     */
    Command commandType() throws Exception {


        if (currentCommand.matches(BLANK_LINE_REGEX) || currentCommand.matches(COMMENT_LINE_REGEX)) {
            return null;
        } else if (currentCommand.matches(L_INSTRUCTION_REGEX)) {
            return Command.L_COMMAND;
        } else if (currentCommand.matches(A_INSTRUCTION_REGEX)) {
            return Command.A_COMMAND;
        } else if (currentCommand.matches(C_INSTRUCTION_REGEX)) {
            return Command.C_COMMAND;
        } else {
            throw new Exception("Invalid command on line" + lineNumber);
        }

    }

    /**
     * Provides the symbol for the current A or L command.
     *
     * @return the symbol or decimal Xxx of the current command @Xxx or (Xxx).
     * Should be called only when commandType() is A_COMMAND or L_COMMAND.
     * @throws Error when commandType is not A or L
     */

    String symbol() throws Error, Exception {
        StringBuilder sb = new StringBuilder();
        int idx = 1; // The first char will not be part of the symbol - will be either @ or (
        if (commandType() == Command.A_COMMAND) {
            while(idx < currentCommand.length()){
                if (currentCommand.charAt(idx) == '/' || currentCommand.charAt(idx) == ' '){
                    break;
                }
                sb.append(currentCommand.charAt(idx));
                idx++;
            }

        } else if (commandType() == Command.L_COMMAND) {
            while (currentCommand.charAt(idx) != ')') {
                sb.append(currentCommand.charAt(idx));
                idx++;
            }
        } else {
            throw new Error("Not an A or L command");
        }
        return sb.toString();

    }

    /**
     * Gets the destination mnemonic from the current C command (dest=comp;jump)
     *
     * @return the dest mnemonic in the current C-command (8 possibilities). Should
     * be called only when commandType() is C_COMMAND. Returns null if no
     * dest mnemonic.
     * @throws Error if not a C command.
     */

    String dest() throws Error, Exception {
        if (commandType() != Command.C_COMMAND) {
            throw new Error("Not a C command");
        }
        if (!currentCommand.contains("=")) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while (currentCommand.charAt(idx) != '=') {
            sb.append(currentCommand.charAt(idx));
            idx++;
        }
        return sb.toString();
    }

    /**
     * Gets the comp mnemonic in the current C command (dest =comp;jump).
     *
     * @return the comp mnemonic in the current C-command (28 possibilities). Should
     * be called only when commandType() is C_COMMAND.
     * @throws Error if not a C command.
     */
    String comp() throws Error, Exception {

        // REFACTOR THIS METHOD

        if (commandType() != Command.C_COMMAND) {
            throw new Error("Not a C command");
        }
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        // All commands are of form Dest = comp;jump, comp;jump or Dest = comp

        // dest = comp;jump
        if (currentCommand.contains("=") && currentCommand.contains(";")) {
            idx = currentCommand.indexOf("=") + 1;
            while (currentCommand.charAt(idx) != ';') {
                sb.append(currentCommand.charAt(idx));
                idx++;
            }
        }
        // dest = comp
        else if (currentCommand.contains("=")) {
            idx = currentCommand.indexOf("=") + 1;
            while (idx < currentCommand.length()) {
                if(currentCommand.charAt(idx) == '/'){
                    break;
                }
                sb.append(currentCommand.charAt(idx));
                idx++;
            }
        }
        // comp;jump
        else {
            while (currentCommand.charAt(idx) != ';') {
                sb.append(currentCommand.charAt(idx));
                idx++;
            }
        }
        return sb.toString();
    }

    /**
     * Gets the jump mnemonic in the current C command (dest =comp;jump).
     *
     * @return the jump mnemonic in the current C-command (8 possibilities). Should
     * be called only when commandType() is C_COMMAND. Returns null if no
     * jump component.
     * @throws Error if not a C command.
     */

    String jump() throws Error, Exception {
        if (
                commandType() != Command.C_COMMAND) {
            throw new Error("Not a C command");
        }
        if (!currentCommand.contains(";")) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int idx = currentCommand.indexOf(";");
        // Add the next 3 characters - all jump mnemonics are 3 characters long.
        sb.append(currentCommand.charAt(idx + 1));
        sb.append(currentCommand.charAt(idx + 2));
        sb.append(currentCommand.charAt(idx + 3));
        return sb.toString();
    }
}