package fish.payara.extras.diagnostics.collection.collectors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.logging.LogLevel;

public class LogCollector extends FileCollector {

    @Override
    public int collect() {
        ParameterMap params = getParams();
        if(params != null) {
            String logPathString = params.getOne("LogPath");
            String outputPathString = params.getOne("outputDir");
            if(logPathString != null) {
                Path logPath = Path.of(logPathString);
                Path outputPath = Path.of(outputPathString);
                if(confirmPath(logPath, false) && confirmPath(outputPath, true)) {
                    try {
                        Files.walkFileTree(logPath, new CopyDirectoryVisitor(outputPath));
                    } catch(IOException io) {
                        logger.log(LogLevel.SEVERE, "Could not copy directory " + logPathString + " to path " + outputPathString);
                        io.printStackTrace();
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    private class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {

        private final Path destination;
        private Path path = null;

        public CopyDirectoryVisitor(Path destination) {
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if(path == null) {
                this.path = dir;
            } else {
                Files.createDirectory(destination.resolve(path.relativize(destination)));
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, destination.resolve(path.relativize(file)));
            return FileVisitResult.CONTINUE;
        }
    }
}
