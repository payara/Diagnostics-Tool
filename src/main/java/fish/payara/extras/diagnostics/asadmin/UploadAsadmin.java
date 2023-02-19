package fish.payara.extras.diagnostics.asadmin;

import java.io.FileNotFoundException;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import fish.payara.extras.diagnostics.upload.UploadService;

@Service(name = "upload")
@PerLookup
public class UploadAsadmin extends BaseAsadmin {

    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "user_password";
    private static final String DESTINATION_PARAM = "destination";
    private static final String FILE_PATH = "filePath";

    private static final String[] PARAMETER_OPTIONS = {USERNAME_PARAM, PASSWORD_PARAM, DESTINATION_PARAM, FILE_PATH};

    @Param(name = USERNAME_PARAM, shortName = "u", optional = false)
    private String username;

    @Param(name = PASSWORD_PARAM, shortName = "p", optional = false, password = true, alias = "nexusPassword")
    private String password;

    @Param(name = DESTINATION_PARAM, shortName = "d", optional = false, acceptableValues = "nexus, zendesk")
    private String destination;

    @Param(name = FILE_PATH, shortName = "f", optional = false)
    private String filePath;

    private UploadService uploadService;

    @Override
    protected int executeCommand() throws CommandException {
        parameterMap = populateParameters(new ParameterMap());

        uploadService = new UploadService(parameterMap, PARAMETER_OPTIONS);

        try {
            uploadService.executeUpload();
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } 

        return 0;
    }

    private ParameterMap populateParameters(ParameterMap params) throws CommandException {
        for(String opt : PARAMETER_OPTIONS) {
            params.add(opt, getOption(opt));
        }

        return params;
    }
    
}
