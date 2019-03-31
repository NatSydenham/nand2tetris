
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.security.InvalidParameterException;
import java.util.Scanner;

/**
 * Encapsulates access to the input code. Reads a VM command, parses it, and provides convenient access to
 * its components. In addition, Removes all white space and comments.
 */
public class Parser {

    private Scanner scanner;
    private String currentCommand;
    private File inFile;
    private final String COMMENT_LINE_REGEX = "^\\/\\/.*$";
    private final String BLANK_LINE_REGEX = "^\\s*$";
    private final String PUSH_REGEX = "^push (local|argument|static|this|that|constant|pointer|temp) [0-9]+$";
    private final String POP_REGEX = "^pop (local|argument|static|this|that|pointer|temp) [0-9]+$";
    private final String ARITHMETIC_REGEX = "^(add|sub|neg|eq|lt|gt|and|or|not)$";
    private int lineNumber;

    /**
     * Opens the input file/stream and gets ready
     * to parse it.
     *
     * @param filePath The path to the input file.
     * @throws FileNotFoundException if input filepath is incorrect.
     */
    public Parser(String filePath) throws FileNotFoundException {
        this.inFile = new File(filePath);
        this.lineNumber = 0;
        this.scanner = new Scanner(inFile);

    }

    /**
     * @return true if there are more commands in the input file, otherwise, return false.
     */
    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command from the input and
     * makes it the current command. Should be
     * called only if hasMoreCommands() is
     * true. Initially there is no current command.
     */
    public void advance() {
        currentCommand = scanner.nextLine().trim();
        lineNumber++;
    }

    /**
     * Returns the type of the current command or throws an InvalidParameterException,
     * specifying the line number and file where said exception occurs.
     *
     * @return the type of the current command. C_ARITHMETIC is returned for all the
     * arithmetic VM commands.
     * @throws InvalidParameterException if command is not a valid command as per VM language specification.
     */
    public Command commandType() {
        if (currentCommand.matches(COMMENT_LINE_REGEX) || currentCommand.matches(BLANK_LINE_REGEX)) {
            return null;
        } else if (currentCommand.matches(PUSH_REGEX)) {
            return Command.C_PUSH;
        } else if (currentCommand.matches(POP_REGEX)) {
            return Command.C_POP;
        } else if (currentCommand.matches(ARITHMETIC_REGEX)) {
            return Command.C_ARITHMETIC;
        } else {
            throw new InvalidParameterException("Invalid command on line " + lineNumber + " of " + inFile.toString());
        }
    }

    /**
     * @return the first argument of the current
     * command. In the case of C_ARITHMETIC,
     * the command itself (“add”, “sub”, etc.) is
     * returned. Should not be called for
     * C_RETURN.
     */
    public String arg1() throws InvalidObjectException {
        if (commandType() == Command.C_RETURN) {
            throw new InvalidObjectException("Method arg1 should not be called when commandType() returns C_RETURN");
        }
        if (commandType() == Command.C_ARITHMETIC) {
            return currentCommand;
        } else {
            return currentCommand.split(" ")[1];
        }
    }

    /**
     * @return the second argument of the current
     * command. Should be called only if the
     * current command is C_PUSH, C_POP,
     * C_FUNCTION, or C_CALL.
     */
    public String arg2() throws InvalidObjectException {
        Command commandType = commandType();
        if (commandType == Command.C_CALL
                || commandType == Command.C_FUNCTION
                || commandType == Command.C_POP
                || commandType == Command.C_PUSH) {
            return currentCommand.split(" ")[2];

        } else {
            throw new InvalidObjectException("Method arg2 should only be called when commandType() returns C_CALL, " +
                    "C_POP, " +
                    "C_PUSH " +
                    "or C_FUNCTION");
        }
    }
}
