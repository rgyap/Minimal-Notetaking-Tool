package src.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public final class Write {
    private Write() {
    }

    public static void writeToHTML(Path path, Path targetPath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("src/core/template/note_template.html"));
		
		String fileName = path.toString();

		ArrayList<ArrayList<Character>> flines = Read.readFileLines(fileName);
        ArrayList<String> converts = Convert.conv(flines, path.toAbsolutePath());
		
		ArrayList<String> styles = Find.getStyleFiles("src/core/template");
		
		String insertContent = stringWithLines(converts);

        StringBuilder result = new StringBuilder();
		

        for (String line : lines) {
            if (line.equals("68747470733A2F2F7777772E796F75747562652E636F6D2F77617463683F763D6451773477395767586351")) {
                result.append(insertContent).append("\n");
            } else if (line.equals("5354594C4553")) {
				for (int i = 0; i < styles.size(); i++) {
					String stylelink = "<link rel='stylesheet' href='" + styles.get(i) + "'>";
					result.append(stylelink).append("\n");
				}
			} else {
                result.append(line).append("\n");
            }
        }
        Files.writeString(targetPath, result.toString());
        
    }
	

	
	public static String stringWithLines(ArrayList<String> s) {
		String res = "";
		for (String line : s) {
			res = res + line + "\n"; 
		}
		return res; 
	}
	
}
