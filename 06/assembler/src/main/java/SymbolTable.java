import java.util.HashMap;

/**
 * SymbolTable: Keeps a correspondence between symbolic labels and numeric
 * addresses.
 */

public class SymbolTable {

    HashMap<String, Integer> symbolTable;

    /**
     * Constructor method which builds the symbol table with the predefined symbols
     * as per the Hack specification. Creates R0-R15, as well as SCREEN, KBD, SP,
     * LCL, ARG, THIS and THAT.
     */

    public SymbolTable() {
        this.symbolTable = new HashMap<String, Integer>();
        for (int i = 0; i < 16; i++) {
            symbolTable.put("R" + i, i);
        }
        this.symbolTable.put("SCREEN", 16384);
        this.symbolTable.put("KBD", 24576);
        this.symbolTable.put("SP", 0);
        this.symbolTable.put("LCL", 1);
        this.symbolTable.put("ARG", 2);
        this.symbolTable.put("THIS", 3);
        this.symbolTable.put("THAT", 4);
    }

    /**
     * Adds the parameters as a key,value pair to the symbol table.
     *
     * @param symbol  a string representing the key to add to the symbol table.
     * @param address an integer value representing the address of the symbol.
     */

    void addEntry(String symbol, int address) {
        this.symbolTable.put(symbol, address);
    }

    /**
     * Checks whether there is an address mapping for the given symbol.
     *
     * @param symbol a string representing the symbol to check.
     * @return whether the symbol table contains a mapping for the given symbol.
     */

    boolean contains(String symbol) {
        boolean result = this.symbolTable.containsKey(symbol) ? true : false;
        return result;
    }

    /**
     * Returns the value of a given key in the symbol table, representing an address
     * in memory. Should only be called when there is a confirmed mapping.
     *
     * @param symbol The symbol in the symbol table.
     * @return the address of the symbol.
     */

    int getAddress(String symbol) {
        return symbolTable.get(symbol);
    }

}