import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyser {


    private List<String> processArgsTokenise(String[] args) throws FileNotFoundException {
        if (args.length > 1) {
            throw new InvalidParameterException("Expected only one ARG - provide a directory or a single .jack file.");
        }
        File file = new File(args[0]);
        List<String> paths = new ArrayList<>();
        if (file.isDirectory()) {
            for (File inputs : file.listFiles()) {
                if (inputs.getPath().endsWith(".jack")) {
                    paths.add(inputs.getPath());
                }
            }
            if (paths.size() == 0) {
                throw new FileNotFoundException("No .jack files in directory");
            }
        } else if (file.isFile() && file.getName().endsWith(".jack")) {
            paths.add(file.getPath());
        } else {
            throw new FileNotFoundException("Not a valid file or directory");
        }
        return paths;

    }

    private void tokenise(List<String> paths) throws Exception {
        for (String path : paths) {
            JackTokeniser tokeniser = new JackTokeniser(path);
            File outFile = new File(path.replace(".jack", "T.xml"));
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            writer.println("<tokens>");
            while (tokeniser.hasMoreTokens()) {
                tokeniser.advance();
                Token type = tokeniser.tokenType();
                switch (type) {
                    case KEYWORD:
                        writer.println("<keyword> " + tokeniser.keyWord() + " </keyword>");
                        break;
                    case SYMBOL:
                        writer.println("<symbol> " + tokeniser.getSym() + " </symbol>");
                        break;
                    case WHITESPACE:
                        break;
                    case STRING_CONST:
                        writer.println(("<stringConstant> " + tokeniser.stringVal() + " </stringConstant>").replaceAll("\"", ""));
                        break;
                    case INT_CONST:
                        writer.println("<integerConstant> " + tokeniser.intVal() + " </integerConstant>");
                        break;
                    case IDENTIFIER:
                        writer.println("<identifier> " + tokeniser.getIdent() + " </identifier>");
                }
            }
            writer.println("</tokens>");
            writer.flush();
            writer.close();
        }
    }

    // Gets a new list of paths for CompilationEngine input files.
    private List<String> getXmlPaths(List<String> paths) throws Exception {
        List<String> xmlIn = new ArrayList<>();
        for (String path : paths) {
            xmlIn.add(path.replace(".jack", "T.xml"));
        }
        return xmlIn;
    }


    private void compile(List<String> paths) throws Exception {
        for (String path : paths) {
            CompilationEngine compilationEngine = new CompilationEngine(path, path.replace("T.xml", ".xml"));
            compilationEngine.compileClass();
            compilationEngine.close();
        }
    }

    public static void main(String[] args) throws Exception {
        JackAnalyser analyser = new JackAnalyser();
        List<String> paths = analyser.processArgsTokenise(args);
        System.out.println("Tokenising...");
        analyser.tokenise(paths);
        System.out.println("Tokenisation completed successfully...");
        paths = analyser.getXmlPaths(paths);
        System.out.println("Analysing syntax...");
        analyser.compile(paths);
        System.out.println("Syntax Analysis complete");
    }
}
