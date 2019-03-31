
import java.io.*;
import java.security.InvalidParameterException;
import java.util.HashMap;


/**
 * Translates the VM commands into Assembly language.
 */
public class CodeWriter {

    private File outFile;
    private PrintWriter writer;
    private HashMap<String, String> segmentMap;
    private int jmpIdx;


    /**
     * Opens the output file/stream and gets ready
     * to write into it.
     *
     * @param outPath the path to the output file.
     */
    public CodeWriter(String outPath) throws IOException {
        this.outFile = new File(outPath + ".asm");
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
        this.segmentMap = new HashMap<String, String>();
        this.jmpIdx = 0;
        segmentMap.put("local", "LCL");
        segmentMap.put("argument", "ARG");
        segmentMap.put("this", "THIS");
        segmentMap.put("that", "THAT");
        segmentMap.put("pointer", "R3");
        segmentMap.put("temp", "R5");
    }

    /**
     * Informs the CodeWriter that the translation of a new VM file has started.
     * UNIMPLEMENTED FOR PROJECT 07
     */
    public void setFileName(String fileName) {

    }

    /**
     * Writes the assembly code that is the
     * translation of the given arithmetic command.
     * <p>
     * General algorithm for binary operations:
     * 1. Pop top 2 values from stack.
     * 2. Compute operation.
     * 3. Push to stack.
     * <p>
     * General algorithm for unary operations:
     * 1. Pop top value from stack.
     * 2. Compute operation.
     * 3. Push to stack.
     *
     * @param command the given arithmetic command.
     */
    public void writeArithmetic(String command) {
        writer.println("// " + command);
        if (command.equals("add")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            writer.println("M=D+M");
        } else if (command.equals("sub")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            writer.println("M=M-D");
        } else if (command.equals("neg")) {
            writer.println("@SP");
            writer.println("A=M-1");
            writer.println("M=-M");
        } else if (command.equals("eq") || command.equals("lt") || command.equals("gt")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            writer.println("D=M-D");
            if (command.equals("eq")) {
                writer.println("@EQUAL_" + jmpIdx);
                writer.println("D;JEQ");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=0");
                writer.println("@PUSH_EQUAL" + jmpIdx);
                writer.println("0;JMP");
                writer.println("(EQUAL_" + jmpIdx + ")");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=-1");
                writer.println("@PUSH_EQUAL" + jmpIdx);
                writer.println("0;JMP");
                writer.println("(PUSH_EQUAL" + jmpIdx + ")");
            } else if (command.equals("lt")) {
                writer.println("@LESS_THAN_" + jmpIdx);
                writer.println("D;JLT");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=0");
                writer.println("@PUSH_LESS" +jmpIdx);
                writer.println("0;JMP");
                writer.println("(LESS_THAN_" + jmpIdx + ")");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=-1");
                writer.println("@PUSH_LESS" + jmpIdx);
                writer.println("0;JMP");
                writer.println("(PUSH_LESS" + jmpIdx + ")");
            } else {
                writer.println("@GREATER_THAN_" + jmpIdx);
                writer.println("D;JGT");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=0");
                writer.println("@PUSH_GREATER" + jmpIdx);
                writer.println("0;JMP");
                writer.println("(GREATER_THAN_" + jmpIdx + ")");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=-1");
                writer.println("@PUSH_GREATER" + jmpIdx);
                writer.println("0;JMP");
                writer.println("(PUSH_GREATER" + jmpIdx + ")");
            }
            jmpIdx++;
        } else if (command.equals("or") || command.equals("and")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            if (command.equals("or")) {
                writer.println("M=D|M");
            } else {
                writer.println("M=D&M");
            }
        } else if (command.equals("not")) {
            writer.println("@SP");
            writer.println("A=M-1");
            writer.println("M=!M");
        } else {
            throw new InvalidParameterException("Command " + command + " not valid");
        }
    }

    private void pushToStack() {
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M+1");
    }

    private void popFromStack() {
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("M=D");
    }

    /**
     * Writes the assembly code that is the
     * translation of the given command, where
     * command is one of the two enumerated
     * values: C_PUSH or C_POP.
     *
     * @param command the command type, either C_PUSH or C_POP.
     * @param segment the memory segment to push to or pop from.
     * @param index   the location within the segment.
     * @throws IndexOutOfBoundsException if addressing pointer segment and index not in {0,1},
     *                                   or if addressing temp segment and index > 8
     * @throws InvalidParameterException if segment name invalid, or command invalid.
     */
    public void writePushPop(Command command, String segment, int index) {
        boolean localArgThisThat = segment.equals("local")
                || segment.equals("argument")
                || segment.equals("this")
                || segment.equals("that");


        if (command == Command.C_PUSH) {
            writer.println("// push " + segment + " " + index);
            if (localArgThisThat) {
                writer.println("@" + segmentMap.get(segment));
                writer.println("D=M");
                writer.println("@" + index);
                writer.println("A=D+A");
                writer.println("D=M");
                pushToStack();
            } else if (segment.equals("constant")) {
                writer.println("@" + index);
                writer.println("D=A");
                pushToStack();
            } else if (segment.equals("static")) {
                String fileName = outFile.toString().replaceAll(".*/","");
                writer.println("@" + fileName + "." + index);
                writer.println("D=M");
                pushToStack();
            } else if (segment.equals("pointer")) {
                if (index == 0) {
                    writer.println("@THIS");
                } else if (index == 1) {
                    writer.println("@THAT");
                } else {
                    throw new InvalidParameterException("Pointer index can be 0 or 1 only.");
                }
                writer.println("D=M");
                pushToStack();
            } else if (segment.equals("temp")) {
                writer.println("@R5");
                writer.println("D=A");
                writer.println("@" + index);
                writer.println("D=D+A");
                writer.println("D=M");
                pushToStack();

            } else {
                throw new InvalidParameterException("Invalid segment name");
            }
        } else if (command == Command.C_POP) {
            writer.println("// pop " + segment + " " + index);
            if (localArgThisThat) {
                writer.println("@" + segmentMap.get(segment));
                writer.println("D=M");
                writer.println("@" + index);
                writer.println("D=D+A");
                popFromStack();
            }else if(segment.equals("temp")){
                writer.println("@" + segmentMap.get(segment));
                writer.println("D=A");
                writer.println("@" + index);
                writer.println("D=D+A");
                popFromStack();
            }
            else if (segment.equals("static")) {
                String fileName = outFile.toString().replaceAll(".*/","");
                writer.println("@" + fileName + "." + index);
                writer.println("D=A");
                popFromStack();
            } else if (segment.equals("pointer")) {
                if (index == 1) {
                    writer.println("@THAT");
                } else if (index == 0) {
                    writer.println("@THIS");
                } else {
                    throw new IndexOutOfBoundsException("Pointer segment can only access index in {0, 1}");
                }
                writer.println("D=A");
                popFromStack();
            } else {
                throw new InvalidParameterException("Invalid segment name");
            }
        } else {
            throw new InvalidParameterException("Command must be C_PUSH or C_POP only.");
        }
    }

    /**
     * Closes the output file.
     */
    public void close() {
        writer.flush();
        writer.close();
    }
}
