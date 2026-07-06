package mortis.speech;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.nio.file.Path;

import mortis.utils.Env;

// public class VoiceManager {
//     private final String path = Env.get("MORTIS_SPEECH_TEXT_PATH", Path.of(System.getProperty("user.home"), "output.txt").toString());
//     public String getTextFromSpeech() {
//         StringBuilder data = new StringBuilder();
//         File output = new File(path);

//         try (Scanner scanner = new Scanner(output)) {
//             while (scanner.hasNextLine()) {
//                 data.append(scanner.nextLine());
//                 System.out.println(data);
//             }
//         } catch (FileNotFoundException e) {
//             System.out.println("An error occurred.");
//             e.printStackTrace();
//         }
        
//         return data.toString();
//     }
// }