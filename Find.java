import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;

import java.io.IOException;

public class Find {
    
    public static String findFile(Path dir, String fileName)  {

        if (!Files.isDirectory(dir)) {
            return "";
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {

                if (Files.isDirectory(entry)) {
                    String result = findFile(entry, fileName);
                    if (!result.isEmpty()) {
                        return result;
                    }
                } else if (entry.getFileName().toString().equals(fileName)) {
                    return entry.toAbsolutePath().toString();
                }
            }
        } catch (Exception e) {
            return "";
        }

        return "";
    }
}
