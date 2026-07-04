package src;

import java.nio.file.Path;
import java.nio.file.Paths;

import src.cli.NoteProcessor;
import src.cli.NoteManager;

public class Main {

	public static void main(String[] args) {
        Path target = Paths.get("src/../mainnotes");
        Path source = Paths.get("src/../notes"); 
		int indentspaces = 2;

		NoteProcessor np = new NoteProcessor(source, target, indentspaces);
		np.issueCommand(args);
	}

}
