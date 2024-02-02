package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.collection.Collector;
import org.glassfish.api.logging.LogLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public abstract class FileCollector implements Collector {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private Path filePath;
    private Path destination;
    private String instanceName;

    private Map<String, String> params;

    public FileCollector() {}

    public FileCollector(String filePath, String destination) {
        this(Paths.get(filePath), Paths.get(destination));
    }

    public FileCollector(Path filePath, Path destination) {
        this.filePath = filePath;
        this.destination = destination;
    }

    public FileCollector(File file, String destination) {
        this(file.getAbsolutePath(), destination);
    }

    @Override
    public int collect() {
        try {
            if (confirmPath(filePath, false) && confirmPath(destination, true)) {
                if (instanceName != null) {
                    Files.copy(filePath, destination.resolve((instanceName + "-" + filePath.getFileName())), REPLACE_EXISTING);
                } else {
                    Files.copy(filePath, destination.resolve(filePath.getFileName()), REPLACE_EXISTING);
                }
            }
        } catch (IOException ie) {
            logger.log(LogLevel.SEVERE, "Could not copy path from " + filePath + " to " + destination);
            ie.printStackTrace();
            return 1;
        }

        return 0;
    }

    protected boolean confirmPath(Path path, boolean createIfNonExistant) {
        if (path != null) {
            if (Files.exists(path)) {
                return true;
            } else {
                if (createIfNonExistant) {
                    logger.log(LogLevel.INFO, "Attempting to create missing path at " + path);
                    try {
                        Files.createDirectory(path);
                    } catch (IOException io) {
                        logger.log(LogLevel.WARNING, "Could not create file at " + path.toString());
                        return false;
                    }
                    //Path is confirmed if it exists.
                    return Files.exists(path);
                }
            }
        }
        return false;
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, String> params) {
        if (params != null) {
            this.params = params;
        }
    }

    public void setFilePath(Path filePath) {
        if (filePath != null) {
            this.filePath = filePath;
        }
    }

    public void setDestination(Path path) {
        if (path != null) {
            this.destination = path;
        }
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public Path getDestination() {
        return this.destination;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
