import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {

        Path sourceRoot = Paths.get("notes");
        Path targetRoot = Paths.get("mainnotes"); 
		
        if (Files.exists(targetRoot)) {
            deleteContents(targetRoot);
        }

        Files.walk(sourceRoot).forEach(sourcePath -> {
            try {
                Path relativePath = sourceRoot.relativize(sourcePath);

                if (Files.isDirectory(sourcePath)) {
                    Path targetPath = targetRoot.resolve(relativePath);
                    Files.createDirectories(targetPath);
                } else {
                    Path targetDir = targetRoot.resolve(relativePath).getParent();
                    Files.createDirectories(targetDir);
                    
                    String originalName = sourcePath.getFileName().toString();

                    if (!(originalName.substring(originalName.length()-2).equals("md"))) {
                        return;
                    }

                    String newName = originalName.substring(0, originalName.length() - 3) + ".html";

                    Path targetPath = targetDir.resolve(newName);

                    System.out.print("Processing " + sourcePath.toString() + "... ");
					long startTime = System.nanoTime();
					Write.writeToHTML(sourcePath, targetPath);
					long endTime = System.nanoTime();
					double duration = (endTime - startTime) / 1000000d;
					System.out.print("Done. (Time taken: "+duration+" millisecond(s))\n");
                }

            } catch (IOException e) {
				System.out.println("Uh oh!");
                e.printStackTrace();
            }
        });
         
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }

    
    private static void deleteContents(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            return;
        }

        try (DirectoryStream<Path> entries = Files.newDirectoryStream(folder)) {
            for (Path entry : entries) {
                deleteRecursively(entry);
            }
        }
    }

}
