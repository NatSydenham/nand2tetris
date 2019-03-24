
import java.io.*;
import java.security.InvalidParameterException;



public class Assembler {

    private BufferedWriter writer;
    private File outputFile;

    public Assembler(String filePath) throws IOException {
        this.outputFile = new File(filePath + ".hack");
        this.writer = new BufferedWriter(new FileWriter(outputFile));
    }

    /**
     * Check whether a String can be represented as an int. Used to determine whether A instruction is a symbol
     * or an integer.
     *
     * @param input the string you wish to check if can be represented as int.
     * @return whether the supplied String can be represented as an int.
     */

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Turns an A instruction symbol to 15-bit binary value
     *
     * @param input the integer you wish to convert to binary.
     * @return the converted String of bits.
     */

    private String to15bitBin(int input) {
        System.out.println(input);
        String bin = Integer.toBinaryString(input);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (15 - bin.length()); i++) {
            sb.append(0);
        }
        sb.append(bin);

        return sb.toString();
    }


    /**
     * Validates command line arguments for proper input.
     *
     * @throws InvalidParameterException when not one argument, and when not a .asm
     *                                   file.
     */

    private static void processArgs(String[] args) throws InvalidParameterException {
        if (args.length != 1) {
            throw new InvalidParameterException("One argument expected");
        }
        if (!args[0].matches("^.+\\.asm$")) {
            throw new InvalidParameterException("Expected .asm file only");
        }
    }

    /**
     * The first pass of the assembler adds each (LABEL) declaration in the .asm
     * file to the symbol table.
     *
     * @param parser      A parser used to march through the .asm file table, keep
     *                    track of the number of instructions, and determine the
     *                    type of instruction on each line.
     * @param symbolTable The mutable symbol table which will be updated with
     *                    (LABEL) references through this pass.
     */

    private SymbolTable firstPass(Parser parser, SymbolTable symbolTable) throws Exception {
        int numInstructions = 0;
        while (parser.hasMoreCommands()) {
            parser.advance();
            if (parser.commandType() == Command.L_COMMAND) {
                symbolTable.addEntry(parser.symbol(), numInstructions);
            } else if (parser.commandType() == Command.A_COMMAND || parser.commandType() == Command.C_COMMAND) {
                numInstructions++;
            }
        }
        return symbolTable;
    }

    /**
     * The second pass of the assembler takes each instruction and converts it to binary. If it finds a symbol,
     * if the symbol is present in the symbol table, it will look up the symbol and replace the instruction with
     * the symbol. If it is not present, the symbol and its associated value will be added to the table.
     *
     * @param parser      The parser used to march through the instructions,
     *                    determine the type of instruction on each line, and provide symbols etc.
     * @param symbolTable The updated symbolTable from the first pass.
     */

    private void secondPass(Parser parser, SymbolTable symbolTable) throws Exception {
        // empty RAM slots start at 16.
        int ram = 16;
        while (parser.hasMoreCommands()) {
            parser.advance();
            System.out.println(parser.currentCommand);

            if (parser.commandType() == Command.A_COMMAND) {
                String bitString;
                writer.write("0");
                String val = parser.symbol();
                if (isInteger(val)) {
                    bitString = to15bitBin(Integer.parseInt(val));
                } else if (symbolTable.contains(val)) {
                    int addr = symbolTable.getAddress(val);
                    bitString = to15bitBin(addr);
                } else {
                    int addr = ram;
                    symbolTable.addEntry(val, addr);
                    ram += 1;
                    bitString = to15bitBin(addr);
                }
                for (int i = 0; i < bitString.length(); i++) {
                    writer.write(bitString.charAt(i));
                }
                writer.newLine();
            }

            else if (parser.commandType() == Command.C_COMMAND) {
                writer.write("111");
                Code comp = new Code(parser.comp().trim());
                String compBits = comp.comp();
                writer.write(compBits);

                Code dest = new Code(parser.dest());
                if (parser.dest() == null) {
                    writer.write("000");
                } else {
                    String destBits = dest.dest();
                    writer.write(destBits);
                }

                Code jump = new Code(parser.jump());
                String jumpBits = jump.jump();
                writer.write(jumpBits);
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }

    /**
     * Main method handles method calls for assembler program and handles IO
     *
     * @param args A path to a single .asm file only.
     */
    public static void main(String[] args) throws Exception {
        processArgs(args);
        Assembler assembler = new Assembler(args[0].substring(0, args[0].lastIndexOf('.')));
        Parser parser = new Parser(args[0]);
        SymbolTable symbolTable = new SymbolTable();
        parser.createNewScanner();
        symbolTable = assembler.firstPass(parser, symbolTable);
        parser.resetLineNumber();
        parser.createNewScanner();
        assembler.secondPass(parser, symbolTable);
        System.out.println("Assembly completed successfully!");
    }

}