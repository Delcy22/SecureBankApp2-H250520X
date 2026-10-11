import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Text-file persistence. Writes go to a temp file first, then are moved atomically to avoid corruption. */
public class FileStorage {
    private final Path dir;

    public FileStorage(String directory) {
        this.dir = Paths.get(directory);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data directory", e);
        }
    }

    public <T> List<T> load(String file, Function<String, T> parser) {
        List<T> items = new ArrayList<>();
        Path p = dir.resolve(file);
        if (!Files.exists(p)) return items;
        try {
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                try {
                    items.add(parser.apply(line));
                } catch (RuntimeException e) {
                    System.err.println("Skipping corrupt line in " + file);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + file);
        }
        return items;
    }

    public void save(String file, List<String> lines) {
        Path target = dir.resolve(file);
        Path tmp = dir.resolve(file + ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Could not save " + file);
        }
    }
}
