package fish.payara.extras.diagnostics.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.glassfish.api.admin.ParameterMap;

import fish.payara.extras.diagnostics.upload.uploaders.NexusAPI;

public class UploadService {
    private ParameterMap params;
    
    public UploadService(ParameterMap params, String[] parameterOptions) {
        this.params = params;
    }

    public int executeUpload() throws FileNotFoundException {
        NexusAPI nexusAPI = null;

        if(params != null) {
            String filePathString = params.getOne("filePath");
            String username = params.getOne("username");
            String password = params.getOne("password");

            if(filePathString != null) {
                Path filePath = Path.of(filePathString);
                if(filePath != null) {
                    if(Files.exists(filePath.normalize())) {
                        File file = new File(filePath.toString());
                        nexusAPI = new NexusAPI(file, username, password);
                    } else {
                        throw new FileNotFoundException("File could not be found at path: " + filePath.toString());
                    }
                }
                
                if(nexusAPI != null) {
                    try {
                        nexusAPI.upload();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return 1;
                }
                
            }
        }

        return 0;
    }

}
