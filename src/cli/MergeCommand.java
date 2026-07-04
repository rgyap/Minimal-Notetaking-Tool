package src.cli;

import java.nio.file.Paths;
import java.nio.file.Path;

public class MergeCommand implements Command {

    private NoteManager nm;

    public MergeCommand(NoteManager nm) {
        this.nm = nm;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Illegal number of arguments");
        }
        nm.merge(args[0], args[1]);
    }
}
