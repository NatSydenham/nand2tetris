

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
    private String currentFile;
    private int jmpIdx;
    private int callRtn;


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
        this.callRtn = 0;
        segmentMap.put("local", "LCL");
        segmentMap.put("argument", "ARG");
        segmentMap.put("this", "THIS");
        segmentMap.put("that", "THAT");
        segmentMap.put("pointer", "R3");
        segmentMap.put("temp", "R5");
    }

    /**
     * Informs the CodeWriter that the translation of a new VM file has started.
     */
    public void setFileName(String fileName) {
        this.currentFile = fileName;
    }

    /**
     * Writes the bootstrap code that initialises the VM. Must be placed at beginning of .asm file.
     * Sets stack pointer to 256, and calls Sys.init
     */
    public void writeInit() {
        writer.println("// Initialisation...");
        writer.println("@256");
        writer.println("D=A");
        writer.println("@SP");
        writer.println("M=D");

        writeCall("Sys.init", 0);
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
        if (command.contains("add")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            writer.println("M=D+M");
        } else if (command.contains("sub")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            writer.println("M=M-D");
        } else if (command.contains("neg")) {
            writer.println("@SP");
            writer.println("A=M-1");
            writer.println("M=-M");
        } else if (command.contains("eq") || command.contains("lt") || command.contains("gt")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            writer.println("D=M-D");
            if (command.contains("eq")) {
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
            } else if (command.contains("lt")) {
                writer.println("@LESS_THAN_" + jmpIdx);
                writer.println("D;JLT");
                writer.println("@SP");
                writer.println("A=M-1");
                writer.println("M=0");
                writer.println("@PUSH_LESS" + jmpIdx);
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
        } else if (command.contains("or") || command.contains("and")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("A=A-1");
            if (command.contains("or")) {
                writer.println("M=D|M");
            } else {
                writer.println("M=D&M");
            }
        } else if (command.contains("not")) {
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
                String fileName = currentFile.replaceAll(".*/", "");
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
            } else if (segment.equals("temp")) {
                writer.println("@" + segmentMap.get(segment));
                writer.println("D=A");
                writer.println("@" + index);
                writer.println("D=D+A");
                popFromStack();
            } else if (segment.equals("static")) {
                String fileName = currentFile.replaceAll(".*/", "");
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
     * Writes the assembly code that is the translation of the label command.
     *
     * @param label the label to write.
     */
    public void writeLabel(String label) {
        writer.println("// label " + label);
        writer.println("(" + label + ")");
    }


    /**
     * Writes the assembly code that is the translation of the goto command.
     * Jumps to a given label.
     *
     * @param label the label to jump to.
     */
    public void writeGoto(String label) {
        writer.println("// goto " + label);
        writer.println("@" + label);
        writer.println("0;JMP");
    }

    /**
     * Writes the assembly code that is the translation of the if-goto command.
     * Pops the top value off the stack, and jumps to label if value is not 0.
     *
     * @param label the label to jump to.
     */
    public void writeIfGoto(String label) {
        writer.println("// if-goto" + label);
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@" + label);
        writer.println("D;JNE");

    }


    /**
     * Writes the assembly code that is the translation of the call command.
     * Pushes the return address, LCL, ARG, THIS and THAT, then sets ARG to SP-5-nArgs.
     * Finally, sets LCL=SP and goes to the functionName, setting a label for the return address.
     *
     * @param functionName the name of the function to call
     * @param nArgs        the number of arguments the function takes.
     */
    public void writeCall(String functionName, int nArgs) {
        writer.println("// call " + functionName + " " + nArgs);
        writer.println("@returnaddr" + functionName + callRtn);
        writer.println("D=A");
        pushToStack();
        writer.println("@LCL");
        writer.println("D=M");
        pushToStack();
        writer.println("@ARG");
        writer.println("D=M");
        pushToStack();
        writer.println("@THIS");
        writer.println("D=M");
        pushToStack();
        writer.println("@THAT");
        writer.println("D=M");
        pushToStack();
        writer.println("@5");
        writer.println("D=A");
        writer.println("@" + nArgs);
        writer.println("D=D+A");
        writer.println("@SP");
        writer.println("D=M-D");
        writer.println("@ARG");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");
        writer.println("@" + functionName);
        writer.println("0;JMP");
        writer.println("(returnaddr" + functionName + callRtn + ")");
        callRtn++;
    }


    /**
     * Writes the assembly code that is the translation of the function command.
     * Sets a label (Filename.functionName) and pushes 0 numLocals times to the stack.
     *
     * @param functionName the name of the function.
     * @param numLocals    the number of local variables.
     */
    public void writeFunction(String functionName, int numLocals) {
        writer.println("// function " + currentFile + "." + functionName + " " + numLocals);
        writer.println("(" + functionName + ")");
        for (int i = 0; i < numLocals; i++) {
            writer.println("@0");
            writer.println("D=A");
            pushToStack();
        }
    }


    /**
     * Writes the assembly code that is the translation of the return command.
     * Sets frame = local, return = *frame-5, *ARG = pop, SP = ARG+1, restores THIS, THAT, ARG, and LCL,
     * then goes to return address in caller's code.
     */
    public void writeReturn() {
        writer.println("// return ");
        // frame = LCL
        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@frame");
        writer.println("M=D");

        // ret = *(frame-5)
        writer.println("@5");
        writer.println("D=A");
        writer.println("@frame");
        writer.println("A=M-D");
        writer.println("D=M");
        writer.println("@ret");
        writer.println("M=D");

        // *ARG = pop()
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("A=M");
        writer.println("M=D");

        // SP = ARG + 1
        writer.println("@ARG");
        writer.println("D=M+1");
        writer.println("@SP");
        writer.println("M=D");

        // THAT = *(frame-1)
        writer.println("@1");
        writer.println("D=A");
        writer.println("@frame");
        writer.println("A=M-D");
        writer.println("D=M");
        writer.println("@THAT");
        writer.println("M=D");

        // THIS = *(frame-2)
        writer.println("@2");
        writer.println("D=A");
        writer.println("@frame");
        writer.println("A=M-D");
        writer.println("D=M");
        writer.println("@THIS");
        writer.println("M=D");

        // ARG = *(frame-3)
        writer.println("@3");
        writer.println("D=A");
        writer.println("@frame");
        writer.println("A=M-D");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("M=D");

        // LCL = *(frame-4)
        writer.println("@4");
        writer.println("D=A");
        writer.println("@frame");
        writer.println("A=M-D");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");

        // goto ret
        writer.println("@ret");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    /**
     * Closes the output file.
     */
    public void close() {
        writer.flush();
        writer.close();
    }
}
