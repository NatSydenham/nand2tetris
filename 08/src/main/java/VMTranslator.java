
import java.io.File;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class VMTranslator {


    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new InvalidParameterException("Expected one argument only - .vm file or" +
                    " a directory containing at least one .vm file");
        }
        File input = new File(args[0]);
        List<String> paths = new ArrayList<String>();
        CodeWriter codeWriter;
        if (input.isDirectory()) {
            for (File file : input.listFiles()) {
                if (file.getPath().endsWith(".vm")) {
                    paths.add(file.getPath());
                }
            }
            if (paths.size() == 0) {
                throw new InvalidParameterException("No .vm files in directory");
            }
            codeWriter = new CodeWriter(input.getPath() + input.getPath().substring(input.getPath().lastIndexOf("/")));
            codeWriter.writeInit();
        } else if (input.isFile()) {
            if (input.getPath().endsWith(".vm")) {
                paths.add(input.getPath());
                codeWriter = new CodeWriter(paths.get(0).replace(".vm", ""));
            } else {
                throw new InvalidParameterException("Not a .vm file");
            }
        } else {
            throw new InvalidParameterException("Not a .vm file or directory containing .vm files");
        }

        for (String filePath : paths) {
            Parser parser = new Parser(filePath);
            codeWriter.setFileName(filePath.substring(filePath.lastIndexOf("/")+1));
            while (parser.hasMoreCommands()) {
                parser.advance();
                Command type = parser.commandType();
                if (type == null) {
                    continue;
                }
                else if (type == Command.C_RETURN){
                    codeWriter.writeReturn();
                    continue;
                }
                String arg1 = parser.arg1();
                if (type == Command.C_ARITHMETIC) {
                    codeWriter.writeArithmetic(arg1);
                }
                else if (type == Command.C_POP || type == Command.C_PUSH) {
                    String arg2 = parser.arg2();
                    codeWriter.writePushPop(type, arg1, Integer.valueOf(arg2.trim()));
                }
                else if (type == Command.C_FUNCTION){
                    String arg2 = parser.arg2();
                    codeWriter.writeFunction(arg1, Integer.valueOf(arg2.trim()));
                }
                else if (type == Command.C_CALL){
                    String arg2 = parser.arg2();
                    codeWriter.writeCall(arg1,Integer.valueOf(arg2.trim()));
                }
                else if (type == Command.C_IF){
                    codeWriter.writeIfGoto(arg1);
                }
                else if (type == Command.C_GOTO){
                    codeWriter.writeGoto(arg1);
                }
                else if (type == Command.C_LABEL){
                    codeWriter.writeLabel(arg1);
                }
            }
        }
        codeWriter.close();
    }
}
