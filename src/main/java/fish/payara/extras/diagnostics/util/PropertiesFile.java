package fish.payara.extras.diagnostics.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogLevel;

public class PropertiesFile {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private Path path;
    private Properties props;

    public PropertiesFile(Path path) {
        this.path = path;
        this.props = new Properties();
    }   

    public void store(String key, String value) {
        try(OutputStream out = new FileOutputStream(path.toString())) {
            props.setProperty(key, value);
            props.store(out, null);
        } catch(FileNotFoundException fnfe) {
            logger.log(LogLevel.WARNING, "Properties file was not found", path.toString());
        } catch(IOException io) {
            logger.log(LogLevel.SEVERE, String.format("IOException occured trying to store %s and %s", key, value));
            io.printStackTrace();
        }
    }

    public String get(String key) {
        try(InputStream in = new FileInputStream(path.toString())) {
            props.load(in);
            return props.getProperty(key);
        } catch(FileNotFoundException fnfe) {
            logger.log(LogLevel.WARNING, "Properties file was not found", path.toString());
        } catch(IOException io) {
            logger.log(LogLevel.SEVERE, String.format("IOException occured trying to fetch %s", key));
            io.printStackTrace();
        }
        
        return null;
    }

    public Path getPath() {
        return this.path;
    }
}
