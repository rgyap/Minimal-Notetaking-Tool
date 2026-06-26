package src.cli;

public interface Command {
    void execute(String[] args) throws Exception;
}
