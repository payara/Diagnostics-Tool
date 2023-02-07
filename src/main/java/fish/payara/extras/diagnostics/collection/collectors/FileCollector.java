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
        this(Path.of(filePath), Path.of(filePath));
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
        Params = params;
    }

    @Override
    public void setFilePath(String filePath) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDestination(String path) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getFilePath() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getDestination() {
        // TODO Auto-generated method stub
        
    }

}
