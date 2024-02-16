package fish.payara.extras.diagnostics.collection.collectors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.glassfish.api.logging.LogLevel;

import fish.payara.extras.diagnostics.util.ParamConstants;

public class LogCollector extends FileCollector {

    private Path logPath;

    @Override
    public int collect() {
        Map<String, String> params = getParams();
        if (params == null) {
            return 0;
        }
        String logPathString = params.get(ParamConstants.LOGS_PATH);
        String outputPathString = params.get(ParamConstants.DIR_PARAM);
        if (logPathString == null) {
            return 0;
        }
        if (logPath == null) {
            logPath = Paths.get(logPathString);
        }
        Path outputPath = Paths.get(outputPathString);
        if (confirmPath(logPath, false) && confirmPath(outputPath, true)) {
            try {
                CopyDirectoryVisitor copyDirectoryVisitor = new CopyDirectoryVisitor(outputPath);
                copyDirectoryVisitor.setInstanceName(getInstanceName());
                Files.walkFileTree(logPath, copyDirectoryVisitor);
            } catch (IOException io) {
                logger.log(LogLevel.SEVERE, "Could not copy directory " + logPathString + " to path " + outputPathString);
                io.printStackTrace();
                return 1;
            }
        }
        return 0;
    }

    public Path getLogPath() {
        return logPath;
    }

    public void setLogPath(Path logPath) {
        this.logPath = logPath;
    }

    private class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {

        private final Path destination;
        private Path path = null;

        private String instanceName;

        public CopyDirectoryVisitor(Path destination) {
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (path == null) {
                this.path = dir;
            } else {
                Files.createDirectories(destination.resolve(destination));
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

            Path relativePath = path.relativize(file);

            if (instanceName != null) {
                if (relativePath.startsWith(instanceName)) {
                    return FileVisitResult.CONTINUE;
                }
                Files.createDirectories(destination.resolve(path.relativize(file)).getParent());
                String prefix = instanceName + "-";
                if ((prefix + relativePath).startsWith(prefix + instanceName)) {
                    prefix = "";
                }
                Files.copy(file, destination.resolve((prefix + relativePath)));

            } else {
                Files.copy(file, destination.resolve(relativePath));
            }

            return FileVisitResult.CONTINUE;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }
    }
}
