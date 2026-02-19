import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Write {
    private Write() {
    }

    public static void writeToHTML(Path path, Path targetPath) throws IOException {
        String insertContent = Files.readString(path);
        List<String> lines = Files.readAllLines(Path.of("./template/note_template.html"));

        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (line.equals("@overhere!")) {
                result.append(insertContent).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }
        Files.writeString(targetPath, result.toString());
        
    }
}
