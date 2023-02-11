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
        if(confirmPath(filePath, false) && confirmPath(destination, true)) {
            try {
                Files.copy(filePath, destination.resolve(filePath.getFileName()), REPLACE_EXISTING);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return 1;
    }

    private boolean confirmPath(Path path, boolean createIfNonExistant) {
        if(path != null) {
            if(Files.exists(path)) {
                return true;
            } else {
                if(createIfNonExistant) {
                    try {
                        Files.createDirectory(path);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        logger.log(LogLevel.SEVERE, "Could not validate path at: " + path);
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

    public void setFilePath(Path filePath) throws FileNotFoundException {
        if(filePath != null) {
            this.filePath = filePath;
        }
    }

    public void setDestination(Path path) throws FileNotFoundException {
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
