import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public final class Write {
    private Write() {
    }

    public static void writeToHTML(Path path, Path targetPath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("./template/note_template.html"));
		
		String fileName = path.toString();
		ArrayList<ArrayList<Character>> flines = Read.readFileLines(fileName);
        ArrayList<String> converts = Convert.conv(flines);
		String insertContent = stringWithLines(converts);

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
	
	public static String stringWithLines(ArrayList<String> s) {
		String res = "";
		for (String line : s) {
			res = res + line + "\n"; 
		}
		return res; 
	}
}
