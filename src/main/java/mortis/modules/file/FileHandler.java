package mortis.modules.file;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileWriter;
public class FileHandler {
    private String path;


    public FileHandler(String path) { 
        this.path = path;
    }

    public boolean createFile() throws IOException {
        File file = new File(this.path);
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (file.createNewFile()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String readFile()  {
        File file = new File(this.path);
        String content =  "";
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                content += data;
            }  
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return content;
    }

    public void writeFile(String content) {
        try (FileWriter writer = new FileWriter(this.path);){
            writer.write(content);
            writer.close();  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile() {
        File file = new File(this.path); 
        if (file.delete()) { 
            System.out.println("Deleted the file: " + file.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }
}
