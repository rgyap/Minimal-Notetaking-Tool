import java.nio.file.Paths;
import java.nio.file.Path;

public class Presentation {
	
	private final Path SOURCE = Paths.get("notes");
	
	public void issueCommand(String[] args) {
		if (args.length == 0) {
			return;
		}
		
		try {
			String command = args[0];
			int len = args.length;
			
			switch(command) {
				case "build":
					NoteOperations.convertAll(SOURCE);
					break;
				case "search":
					if (len < 2 || len > 3) {
						throw new IllegalArgumentException("Illegal number of arguments.");
					}
					if (len == 3 && (args[2].equals("--sensitive") || args[2].equals("-s"))) {
						NoteOperations.searchAll(SOURCE, args[1], true);
					} else {
						NoteOperations.searchAll(SOURCE, args[1], false);
					}
					break;
				case "merge":
					NoteOperations.merge(args[1], args[2]);
					break;
				default: 
					throw new IllegalArgumentException("Unknown command.");
			}
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}

}