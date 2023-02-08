package fish.payara.extras.diagnostics.collection.collectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import org.glassfish.api.admin.ParameterMap;

import fish.payara.extras.diagnostics.collection.Collector;

public abstract class FileCollector implements Collector {

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
            Files.move(filePath, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
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
