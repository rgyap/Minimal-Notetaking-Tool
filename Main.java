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
        //Path targetRoot = Paths.get("temp");
        Path targetRoot = Paths.get("mainnotes"); 
		
		//Path tempFolder = Paths.get("temp");

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

                    String newName = originalName.substring(0, originalName.length() - 3) + ".html";

                    Path targetPath = targetDir.resolve(newName);

                    System.out.println("Processing " + sourcePath.toString());
					
					Write.writeToHTML(sourcePath, targetPath);

					System.out.println("Done. The HTML files are now in the 'mainnotes' folder.");
                }

            } catch (IOException e) {
				System.out.println("Uh oh!");
                e.printStackTrace();
            }
        });
		/*
        Files.walk(targetRoot).forEach(sourcePath -> {
            try {
                Path relativePath = targetRoot.relativize(sourcePath);

                if (Files.isDirectory(sourcePath)) {
                    Path targetPath = targetRoot2.resolve(relativePath);
                    Files.createDirectories(targetPath);
                } else {
                    Path targetDir = targetRoot2.resolve(relativePath).getParent();
                    Files.createDirectories(targetDir);

                    String originalName = sourcePath.getFileName().toString();

                    String newName = originalName.substring(0, originalName.length() - 8) + ".html";

                    Path targetPath = targetDir.resolve(newName);
                    
                    System.out.println("Creating HTML document for " + sourcePath.toString());
                    Write.writeToHTML(sourcePath, targetPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

   
        Path tempFolder = Paths.get("temp");

        if (Files.exists(tempFolder)) {
            deleteContents(tempFolder);
        }
*/
         
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }

    
    public static void deleteContents(Path folder) throws IOException {
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
