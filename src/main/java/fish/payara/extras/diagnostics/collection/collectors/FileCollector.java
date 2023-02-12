package fish.payara.extras.diagnostics.collection.collectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.logging.LogLevel;

import fish.payara.extras.diagnostics.collection.Collector;

public abstract class FileCollector implements Collector {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private Path filePath;
    private Path destination;

    private ParameterMap Params;

    // java.util.logging.config.file
    // com.sun.enterprise.server.logging.GFFileHandler.file

    public FileCollector() {}

    public FileCollector(String filePath, String destination) {
        this(Path.of(filePath), Path.of(destination));
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
            if(confirmPath(filePath, false) && confirmPath(destination, true)) {
                Files.copy(filePath, destination.resolve(filePath.getFileName()), REPLACE_EXISTING);
                return 1;
            }
        } catch (IOException ie) {
            logger.log(LogLevel.SEVERE, "Could not copy path from " + filePath + " to " + destination);
            ie.printStackTrace();
        }
        
        return 0;
    }

    private boolean confirmPath(Path path, boolean createIfNonExistant) throws IOException {
        if(path != null) {
            if(Files.exists(path)) {
                return true;
            } else {
                if(createIfNonExistant) {
                    logger.log(LogLevel.WARNING, "Attempting to create output path at " + path);
                    Files.createDirectory(path);
                    //Path is confirmed if it exists.
                    return Files.exists(path);
                }
            }
        }
        return false;
    }

    @Override
    public ParameterMap getParams() {
        return Params;
    }

    @Override
    public void setParams(ParameterMap params) {
        if(params != null) {
            Params = params;
        }
    }

    public void setFilePath(Path filePath) {
        if(filePath != null) {
            this.filePath = filePath;
        }
    }

    public void setDestination(Path path) {
        if(path != null) {
            this.destination = path;
        }
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public Path getDestination() {
        return this.destination;
    }

}
