package fish.payara.extras.diagnostics.asadmin;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

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
    private static final String PASSWORD_PARAM = "userPassword";
    private static final String UPLOAD_DESTINATION_PARAM = "uploadDestination";

    private static final String[] PARAMETER_OPTIONS = {USERNAME_PARAM, PASSWORD_PARAM, UPLOAD_DESTINATION_PARAM, DIR_PARAM};

    @Param(name = USERNAME_PARAM, shortName = "u", optional = false)
    private String username;

    @Param(name = PASSWORD_PARAM, shortName="p", optional = false, password = true)
    private String password;

    @Param(name = UPLOAD_DESTINATION_PARAM, optional = false, acceptableValues = "nexus, zendesk")
    private String destination;

    private UploadService uploadService;

    @Override
    protected int executeCommand() throws CommandException {
        parameterMap = populateParameters(new HashMap<>(), PARAMETER_OPTIONS);

        resolveDir(parameterMap);

        uploadService = new UploadService(parameterMap, PARAMETER_OPTIONS);

        try {
            uploadService.executeUpload();
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } 

        return 0;
    }    
}
