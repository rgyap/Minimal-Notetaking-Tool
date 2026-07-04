package src.cli;

import java.nio.file.Paths;
import java.nio.file.Path;

public class SearchCommand implements Command {
    private NoteManager nm;

    public SearchCommand(NoteManager nm) {
        this.nm = nm;
    }

    @Override
    public void execute(String[] args) throws Exception {

        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Usage: search <query> [--sensitive]");
        }

        boolean sensitive = args.length == 2 && (args[1].equals("--sensitive") || args[1].equals("-s"));

        nm.searchAll(args[0], sensitive);
    }
}
