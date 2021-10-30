package com.github.kmpk;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class FileCreator {
    private FileCreator() {
    }

    public static Path CreateAndPopulateFile(Path file, CharSequence content) throws IOException {
        Files.writeString(file, content);
        return file;
    }

    public static Path CreateAndPopulateFile(Path file, Stream<CharSequence> stream) throws IOException {
        Iterator<CharSequence> iterator = stream.iterator();
        try (final FileWriter writer = new FileWriter(file.toFile(), true);) {
            while (iterator.hasNext()) {
                writer.append(iterator.next()).append('\n');
            }
        }
        return file;
    }
}
