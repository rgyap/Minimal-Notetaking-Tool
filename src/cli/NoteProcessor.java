package src.cli;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.nio.file.Paths;
import java.nio.file.Path;

public class NoteProcessor {

    private final Map<String, Command> commands = new HashMap<>();

    public NoteProcessor(Path noteDir, Path outDir, int indentspaces) {

        NoteManager nm = new NoteManager(noteDir, outDir, indentspaces);

        commands.put("build", new BuildCommand(nm));
        commands.put("search", new SearchCommand(nm));
        commands.put("merge", new MergeCommand(nm));
    }

    public void issueCommand(String[] args) {

        if (args.length == 0) {
            return;
        }

        try {
            Command command = commands.get(args[0]);

            if (command == null) {
                throw new IllegalArgumentException("Unknown command.");
            }

            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
            command.execute(commandArgs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
