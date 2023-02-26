package fish.payara.extras.diagnostics.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogLevel;
import org.glassfish.pfl.basic.logex.Message;

import fish.payara.extras.diagnostics.upload.uploaders.NexusAPI;
import fish.payara.extras.diagnostics.upload.uploaders.ZendeskAPI;
import fish.payara.extras.diagnostics.util.ParamConstants;

public class UploadService {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private Map<String, String> params;

    public UploadService(Map<String, String> params) {
        this.params = params;
    }

    
    /** 
     * Builds and calls upload() for the selected service.
     * 
     * 0 - Success
     * 1 - Failure
     * 
     * @return int
     * @throws FileNotFoundException
     */
    public int executeUpload() throws FileNotFoundException {
        String serviceName = params.get(ParamConstants.UPLOAD_DESTINATION_PARAM);

        if(serviceName == null) {
            logger.log(LogLevel.SEVERE, "A valid service could not be found");
            return 0;
        }

        ServiceAPIBuilder apiBuilder = new ServiceAPIBuilder(serviceName, params);
        Uploader uploader = apiBuilder.getAPI();

        if(uploader != null) {
            return uploader.upload();
        }

        return 1;
    }

    /**
     * Factory class for intialising a service.
     */
    private static class ServiceAPIBuilder {
        Logger logger = Logger.getLogger(this.getClass().getName());

        private String APIName;
        private Map<String, String> params;

        public ServiceAPIBuilder(String APIName, Map<String, String> params) {
            this.APIName = APIName.trim().toLowerCase();
            this.params = params;
        }

        /**
         * Returns the API service based on the APIName.
         * 
         * @return Uploader
         */
        public Uploader getAPI() {
            if (APIName == null || params == null) {
                return null;
            }

            String username = params.get(ParamConstants.USERNAME_PARAM);
            String password = params.get(ParamConstants.PASSWORD_PARAM);
            File file = null;

            boolean valid = false;
            valid = validateParam(ParamConstants.USERNAME_PARAM, params, "Username was not valid or missing. Upload will not continue.");
            valid = validateParam(ParamConstants.USERNAME_PARAM, params, "Password was not valid or missing. Upload will not continue.");

            if(!valid) {
                return null;
            } 

            String pathString = params.get(ParamConstants.DIR_PARAM);
            if (validateParam(ParamConstants.DIR_PARAM, params)) {
                Path path = Path.of(pathString);
                if (Files.exists(path)) {
                    file = new File(pathString);
                }
            }

            if(file == null) {
                if(pathString != null) {
                    logger.log(LogLevel.WARNING, "File {0} could not be found, upload will not continue.", pathString);
                } else {
                    logger.log(LogLevel.WARNING, "File could not be found, upload will not continue.");
                }
                return null;
            }

            switch (APIName) {
                case ParamConstants.NEXUS:
                    return new NexusAPI(file, username, password);
                case ParamConstants.ZENDESK:
                    String ticketNum = params.get(ParamConstants.TICKET_NUM_PARAM);
                    boolean validTicket = validateParam(ParamConstants.TICKET_NUM_PARAM, params, "Ticket number was not correct or non existing.");

                    if(!validTicket) {
                        break;
                    }

                    return new ZendeskAPI(file, username, password, ticketNum);
                default:
                    logger.log(LogLevel.INFO, "Could not find or create service with name: {0}", APIName);
            }

            return null;
        }
        
        /**
         * Returns boolean value if paremters provided are valid.
         * 
         * @param key
         * @param params
         * @param message
         * @return boolean
         */
        private boolean validateParam(String key, Map<String, String> params, String message) {
            if(params == null || key == null) {
                return false;
            } 

            String value = params.get(key);
            if(value == null) {
                if(message != null) {
                    logger.log(LogLevel.WARNING, message);
                } else {
                    logger.log(LogLevel.WARNING, "Parameter {0} was null or was incorrect.", key);
                }
                return false;
            }

            return true;
        }

        /**
         * Overload method which prints a default message.
         * 
         * @param key
         * @param params
         * @return boolean
         */
        private boolean validateParam(String key, Map<String, String> params) {
            return validateParam(key, params, null);
        }

    }
}
