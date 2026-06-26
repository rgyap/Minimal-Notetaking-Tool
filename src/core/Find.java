package src.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;
import java.io.IOException;

import java.util.ArrayList;

public class Find {
	
	private Find() {
		
	}
    
    public static Path findFile(Path dir, String fileName)  {
		// RETURNS ABSOLUTE PATHS OR NULL IF NOT FOUND

        if (!Files.isDirectory(dir)) {
            return null;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    Path result = findFile(entry, fileName);
                    if (result != null) {
                        return result;
                    }
                } else if (entry.getFileName().toString().equals(fileName)) {
					return entry.toAbsolutePath();
                }
            }
        } catch (Exception e) {
			System.out.println(e);
            return null;
        }
        return null;
    }
	
	public static Path find(String dirName, String fileName) { // GIVES A PATH OR NULL IF NOT FOUND
		return findFile(Paths.get(dirName), fileName);
	}
	
	public static ArrayList<String> getStyleFiles(String directoryPath) {

        ArrayList<String> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directoryPath), "*.css")) {
            for (Path entry : stream) {
                result.add(entry.toAbsolutePath().toString());
            }
        } catch (IOException e) {
			System.out.println(e);
			return new ArrayList<String>();
		}

        return result;
    }
	
	public static int[] findStringInFile(Path thing, String query, boolean sensitive) {
		// returns a pair [a,b] where a is the line and b is the column
		// returns null if not found
		if (Files.isDirectory(thing)) {
			return null;
		}
		
		ArrayList<String> thingg = Read.readFileLinesStrings(thing.toString());

		for (int i = 0; i < thingg.size(); i++) {
			String line = thingg.get(i);
			int col;
			if (!sensitive) {
				col = (line.toLowerCase()).indexOf(query.toLowerCase());
			} else {
				col = line.indexOf(query);
			}
			if (col >= 0) {
				int[] out = {i+1, col+1};
				return out;
			}
		}
		return null;
	}
}
