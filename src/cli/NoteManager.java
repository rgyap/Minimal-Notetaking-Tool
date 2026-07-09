package src.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Collections;

import src.core.Find;
import src.core.Read;
import src.core.Write;

public class NoteManager {
    
    private final Path noteDir;
    private final Path outDir;
	private final int indentspaces;

    public NoteManager(Path noteDir, Path outDir, int indentspaces) {
        this.noteDir = noteDir;
        this.outDir = outDir;
		this.indentspaces = indentspaces;
    }
	
    public void searchAll(String query, boolean sensitive) throws IOException {
		Path sourceRoot = this.noteDir;

        ArrayList<String> names = new ArrayList<String>();
		HashMap<String, int[]> locations = new HashMap<String, int[]>();
		
		Files.walk(sourceRoot).forEach(sourcePath -> {
			try {
				if (Files.isDirectory(sourcePath)) {
					return;
				} 
				String s = getFileExtension(sourcePath.toString());
				if (!s.equals("md")) {
					return;
				}
				int[] coords = Find.findStringInFile(sourcePath, query, sensitive);
				if (coords != null) {
					String n = sourcePath.getFileName().toString();
					names.add(n);
					locations.put(n, coords);
				}
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Uh oh!");
			}
		});
		if (names.size() == 0) {
			System.out.println("Not found anywhere.");
			return;
		}
		System.out.println("Found in the following files:");
		Collections.sort(names);
		for (String name : names) {
			int[] coords = locations.get(name);
			System.out.println("- \"" + name + "\" (First occurence: Line " + coords[0] + ", Column " + coords[1] + ")");
		}
	}

    public void merge(String first, String second) throws IOException {
        Path firstp = Find.find(this.noteDir.toString(), first);
        Path secondp = Find.find(this.noteDir.toString(), second);
		
		String extension1 = getFileExtension(first);
		String extension2 = getFileExtension(second);
		
		if (!extension1.equals("md") || !extension2.equals("md")) {
			System.out.println("Invalid merge!");
			return;
		}
		
		String[] resultname = merge(firstp, secondp);
	    	
		System.out.print("Successfully merged \"" + firstp.getFileName().toString() + "\" and \"" +secondp.getFileName().toString() + "\" ");
		System.out.print("into \"" + resultname[0] + "\".\nOutput location: \"" + resultname[1] + "\".\n");
	}

    public void convertAll() throws IOException {
        //Path sourceRoot = this.noteDir; 
		//Path targetRoot = this.outDir;
                
        if (Files.exists(this.outDir)) {
            deleteContents(this.outDir);
        }

        Files.walk(this.noteDir).forEach(sourcePath -> {
            try {
                Path relativePath = this.noteDir.relativize(sourcePath);

                if (Files.isDirectory(sourcePath)) {
                    Path targetPath = this.outDir.resolve(relativePath);
                    Files.createDirectories(targetPath);
					return;
                } 
				
                Path targetDir = this.outDir.resolve(relativePath).getParent();
                Files.createDirectories(targetDir);
                
                String originalName = sourcePath.getFileName().toString();

                if (!(originalName.substring(originalName.length()-2).equals("md"))) {
					Files.copy(sourcePath, targetDir.resolve(originalName), StandardCopyOption.REPLACE_EXISTING);
					return;
                }
				
				// Remove ".md" in the name
                String newName = originalName.substring(0, originalName.length() - 3) + ".html";

                Path targetPath = targetDir.resolve(newName);

                System.out.print("Processing \"" + sourcePath.toString() + "\"... ");
				long startTime = System.nanoTime();
				Write.writeToHTML(sourcePath, targetPath, this.noteDir, this.indentspaces);
				long endTime = System.nanoTime();
				double duration = (endTime - startTime) / 1000000d;
				System.out.print("DONE! (Time: "+duration+" ms)\n");
                
            } catch (IOException e) {
				System.out.println("Uh oh!");
                e.printStackTrace();
            }
        });
	}
	
	public void generateIndexSite() throws IOException {
		Files.walk(this.noteDir).forEach(sourcePath -> {
            try {
                Path relativePath = this.noteDir.relativize(sourcePath);

                if (Files.isDirectory(sourcePath)) {
                    Path targetPath = this.outDir.resolve(relativePath);
                    Files.createDirectories(targetPath);
					return;
                } 
				
                Path targetDir = this.outDir.resolve(relativePath).getParent();
                Files.createDirectories(targetDir);
                
                String originalName = sourcePath.getFileName().toString();

                if (!(originalName.substring(originalName.length()-2).equals("md"))) {
					Files.copy(sourcePath, targetDir.resolve(originalName), StandardCopyOption.REPLACE_EXISTING);
					return;
                }
				
				// Remove ".md" in the name
                String newName = originalName.substring(0, originalName.length() - 3) + ".html";

                Path targetPath = targetDir.resolve(newName);

                System.out.print("Processing \"" + sourcePath.toString() + "\"... ");
				long startTime = System.nanoTime();
				Write.writeToHTML(sourcePath, targetPath, this.noteDir, this.indentspaces);
				long endTime = System.nanoTime();
				double duration = (endTime - startTime) / 1000000d;
				System.out.print("DONE! (Time: "+duration+" ms)\n");
                
            } catch (IOException e) {
				System.out.println("Uh oh!");
                e.printStackTrace();
            }
        });
	}
	
	/*
		HELPER FUNCTIONS
	*/
	
	private String[] merge(Path first, Path second) throws IOException {
		if (Files.isDirectory(first) || Files.isDirectory(second)) {
			return null;
		}
		
		ArrayList<String> file1 = Read.readFileLinesStrings(first.toString());
		ArrayList<String> file2 = Read.readFileLinesStrings(second.toString());
		
		String name1 = first.getFileName().toString();
		String name2 = second.getFileName().toString();
		String firstname = name1.substring(0, name1.length() - 3);
		
		String mergename = firstname + " and " + name2;
		
		StringBuilder result = new StringBuilder();
		
		for (String l : file1) {
			result.append(l + "\n");
		}
		result.append("\n");
		for (String l : file2) {
			result.append(l + "\n");
		}
		Path parent = first.getParent();
		Files.writeString(parent.resolve(mergename), result.toString());
		String[] out = {mergename, parent.toString()};
		return out;
	}
	
	private String getFileExtension(String fileName) {
		int lastIndexOfDot = fileName.lastIndexOf('.');
		if (lastIndexOfDot == -1) {
			return ""; // No extension found
		}
		if (lastIndexOfDot == fileName.length() - 1) {
			return ""; 
		}
		return fileName.substring(lastIndexOfDot + 1);
	}
	
	private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }

    
    private void deleteContents(Path folder) throws IOException {
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
