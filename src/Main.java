package src;

import java.nio.file.Path;
import java.nio.file.Paths;

import src.cli.NoteProcessor;
import src.core.NoteManager;

public class Main {

	public static void main(String[] args) {
        Path target = Paths.get("src/../mainnotes");
        Path source = Paths.get("src/../notes");  


		NoteProcessor np = new NoteProcessor(source, target);
		np.issueCommand(args);
	}

}
