package fish.payara.extras.diagnostics.upload;

import java.io.FileNotFoundException;

import org.glassfish.api.admin.ParameterMap;

public interface Uploader {
    public int upload() throws FileNotFoundException, Exception;
}
