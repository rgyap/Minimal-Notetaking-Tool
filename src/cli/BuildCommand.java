package src.cli;

import java.nio.file.Paths;
import java.nio.file.Path;

import src.core.NoteManager;

public class BuildCommand implements Command {

    private NoteManager nm;

    public BuildCommand(NoteManager nm) {
        this.nm = nm;
    }

    @Override 
    public void execute(String[] args) throws Exception {
        nm.convertAll();
    }
}
