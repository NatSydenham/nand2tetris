import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyser {


    private List<String> processArgs(String[] args) throws FileNotFoundException {
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


    private void compile(List<String> paths) throws Exception {
        for (String path : paths) {
            CompilationEngine compilationEngine = new CompilationEngine(path, path.replace(".jack", ".xml"));
            compilationEngine.compileClass();
            compilationEngine.close();
        }
    }

    public static void main(String[] args) throws Exception {
        JackAnalyser analyser = new JackAnalyser();
        List<String> paths = analyser.processArgs(args);
        analyser.compile(paths);
        System.out.println("Syntax Analysis complete");
    }
}
