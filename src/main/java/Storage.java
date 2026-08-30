import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saves Bob's tasks to the local data file.
 */
public class Storage {
    private static final Path DATA_FILE = Path.of("data", "Bob.txt");

    /**
     * Saves the current tasks, replacing the previous contents of the data file.
     *
     * @param tasks tasks to save
     * @throws IOException if the data directory or file cannot be written
     */
    public static void saveTasks(List<Task> tasks) throws IOException {
        Files.createDirectories(DATA_FILE.getParent());
        List<String> taskLines = tasks.stream().map(Task::toString).collect(Collectors.toList());
        Files.write(DATA_FILE, taskLines, StandardCharsets.UTF_8);
    }
}
