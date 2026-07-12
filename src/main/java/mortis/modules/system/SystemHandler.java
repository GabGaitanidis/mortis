package mortis.modules.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemHandler {
    public static void executeCommand(File file, String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(file);
        pb.start();
    }

    public static void executeCommand( String args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args.split(" "));
        pb.start();
        
        
    }
}
