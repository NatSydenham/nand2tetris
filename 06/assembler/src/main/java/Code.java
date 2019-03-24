import java.util.HashMap;

/**
 * Code: Translates Hack assembly language mnemonics into binary codes.
 */

public class Code {
    private String mnemonic;
    private HashMap<String, Integer[]> compToBin;
    private HashMap<String, Integer[]> jumpToBin;

    /**
     * Constructor takes the piece of the assembly instruction and stores as a
     * String.
     *
     * @param mnemonic the dest, comp, or jump mnemonic to be translated into binary
     */

    public Code(String mnemonic) {
        this.mnemonic = mnemonic;
        this.compToBin = new HashMap<String, Integer[]>();
        this.compToBin.put("0", new Integer[]{0, 1, 0, 1, 0, 1, 0});
        this.compToBin.put("1", new Integer[]{0, 1, 1, 1, 1, 1, 1});
        this.compToBin.put("-1", new Integer[]{0, 1, 1, 1, 0, 1, 0});
        this.compToBin.put("D", new Integer[]{0, 0, 0, 1, 1, 0, 0});
        this.compToBin.put("A", new Integer[]{0, 1, 1, 0, 0, 0, 0});
        this.compToBin.put("!D", new Integer[]{0, 0, 0, 1, 1, 0, 1});
        this.compToBin.put("!A", new Integer[]{0, 1, 1, 0, 0, 0, 1});
        this.compToBin.put("-D", new Integer[]{0, 0, 0, 1, 1, 1, 1});
        this.compToBin.put("-A", new Integer[]{0, 1, 1, 0, 0, 1, 1});
        this.compToBin.put("D+1", new Integer[]{0, 0, 1, 1, 1, 1, 1});
        this.compToBin.put("A+1", new Integer[]{0, 1, 1, 0, 1, 1, 1});
        this.compToBin.put("D-1", new Integer[]{0, 0, 0, 1, 1, 1, 0});
        this.compToBin.put("A-1", new Integer[]{0, 1, 1, 0, 0, 1, 0});
        this.compToBin.put("D+A", new Integer[]{0, 0, 0, 0, 0, 1, 0});
        this.compToBin.put("D-A", new Integer[]{0, 0, 1, 0, 0, 1, 1});
        this.compToBin.put("A-D", new Integer[]{0, 0, 0, 0, 1, 1, 1});
        this.compToBin.put("D&A", new Integer[]{0, 0, 0, 0, 0, 0, 0});
        this.compToBin.put("D|A", new Integer[]{0, 0, 1, 0, 1, 0, 1});
        this.compToBin.put("M", new Integer[]{1, 1, 1, 0, 0, 0, 0});
        this.compToBin.put("!M", new Integer[]{1, 1, 1, 0, 0, 0, 1});
        this.compToBin.put("-M", new Integer[]{1, 1, 1, 0, 0, 1, 1});
        this.compToBin.put("M+1", new Integer[]{1, 1, 1, 0, 1, 1, 1});
        this.compToBin.put("M-1", new Integer[]{1, 1, 1, 0, 0, 1, 0});
        this.compToBin.put("D+M", new Integer[]{1, 0, 0, 0, 0, 1, 0});
        this.compToBin.put("D-M", new Integer[]{1, 0, 1, 0, 0, 1, 1});
        this.compToBin.put("M-D", new Integer[]{1, 0, 0, 0, 1, 1, 1});
        this.compToBin.put("D&M", new Integer[]{1, 0, 0, 0, 0, 0, 0});
        this.compToBin.put("D|M", new Integer[]{1, 0, 1, 0, 1, 0, 1});

        this.jumpToBin = new HashMap<String, Integer[]>();
        this.jumpToBin.put("JGT", new Integer[]{0, 0, 1});
        this.jumpToBin.put("JEQ", new Integer[]{0, 1, 0});
        this.jumpToBin.put("JGE", new Integer[]{0, 1, 1});
        this.jumpToBin.put("JLT", new Integer[]{1, 0, 0});
        this.jumpToBin.put("JNE", new Integer[]{1, 0, 1});
        this.jumpToBin.put("JLE", new Integer[]{1, 1, 0});
        this.jumpToBin.put("JMP", new Integer[]{1, 1, 1});
    }

    /**
     * Converts the dest component of a mnemonic to binary.
     *
     * @return the converted dest instruction.
     */

    String dest() {
        // 3 bit value
        StringBuilder sb = new StringBuilder();
        if (this.mnemonic.contains("A")) {
            sb.append(1);
        } else {
            sb.append(0);
        }
        if (this.mnemonic.contains("D")) {
            sb.append(1);
        } else {
            sb.append(0);
        }
        if (this.mnemonic.contains("M")) {
            sb.append(1);
        } else {
            sb.append(0);
        }
        return sb.toString();
    }

    /**
     * Converts the comp component of a mnemonic to binary.
     *
     * @return the converted comp instruction.
     */
    String comp() {
        // 7 bits
        Integer[] integerArr = this.compToBin.get(this.mnemonic);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < integerArr.length; i++) {
            sb.append(integerArr[i]);
        }
        return sb.toString();
    }

    /**
     * Converts the jump component of a mnemonic to binary.
     *
     * @return the converted jump instruction.
     */

    String jump() {
        String jumpBits = "000";
        if (this.jumpToBin.containsKey(this.mnemonic)) {
            Integer[] integerArray = this.jumpToBin.get(this.mnemonic);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < integerArray.length; i++) {
                sb.append(integerArray[i]);
            }
            return sb.toString();
        }
        return jumpBits;
    }
}