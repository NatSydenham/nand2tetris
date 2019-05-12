import java.io.*;

public class VMWriter {

    /**
     * Writes VM commands into a file. Encapsulates the VM command syntax.
     */

    private PrintWriter writer;
    private File outFile;


    public VMWriter(String outPath) throws IOException {
        this.outFile = new File(outPath);
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
    }

    /**
     * Writes a VM push command
     *
     * @param segment The segment to push to
     * @param index   The index within the segment.
     */
    public void writePush(String segment, int index) {
        System.out.println("Writing " + "push " + segment + " " + index);
        writer.println("push " + segment + " " + index);
    }

    /**
     * Writes a VM pop command
     *
     * @param segment The segment to pop to
     * @param index   The index within the segment.
     */
    public void writePop(String segment, int index) {
        writer.println("pop " + segment + " " + index);
    }

    /**
     * Writes a VM arithmetic command
     *
     * @param command The command to write.
     */
    public void writeArithmetic(String command) {
        System.out.println("writing " + command);
        writer.println(command);
    }

    /**
     * Writes a VM label command
     *
     * @param label The label to write
     */
    public void writeLabel(String label) {
        writer.println("label " + label);
    }

    /**
     * Write a VM Goto command
     *
     * @param label The label to go to.
     */

    public void writeGoto(String label) {
        writer.println("goto " + label);
    }

    /**
     * Writes a VM If-goto command
     *
     * @param label The label to go to.
     */

    public void writeIf(String label) {
        writer.println("if-goto " + label);
    }

    /**
     * Writes a VM call command
     *
     * @param name  The name of the subroutine to call.
     * @param nArgs The number of args in the subroutine.
     */
    public void writeCall(String name, int nArgs) {
        writer.println("call " + name + " " + nArgs);
    }

    /**
     * Writes a VM function command
     *
     * @param name    The name of the function
     * @param nLocals The number of local variables
     */
    public void writeFunction(String name, int nLocals) {
        System.out.println("Writing function: " + name + "with " + nLocals + " locals...");
        writer.println("function " + name + " " + nLocals);
    }

    /**
     * Writes a VM return command
     */
    public void writeReturn() {
        writer.println("return");
    }

    /**
     * Closes the output file & writer
     */
    public void close() {
        writer.flush();
        writer.close();
    }

}
