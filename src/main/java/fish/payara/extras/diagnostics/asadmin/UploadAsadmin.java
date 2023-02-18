package fish.payara.extras.diagnostics.asadmin;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import fish.payara.extras.diagnostics.upload.UploadService;

@Service(name = "upload")
@PerLookup
public class UploadAsadmin extends BaseAsadmin {

    private static final String USERNAME_PARAM = "serverLogs";
    private static final String PASSWORD_PARAM = "domainXml";
    private static final String DESTINATION_PARAM = "outputDir";

    private static final String[] PARAMETER_OPTIONS = {USERNAME_PARAM, PASSWORD_PARAM, DESTINATION_PARAM};

    @Param(name = USERNAME_PARAM, shortName = "u", optional = false)
    private String username;

    @Param(name = PASSWORD_PARAM, shortName = "p", optional = false, password = true)
    private String password;

    @Param(name = DESTINATION_PARAM, shortName = "d", optional = false)
    private String destination;

    private UploadService uploadService;

    @Override
    protected int executeCommand() throws CommandException {
        parameterMap = populateParameters(new ParameterMap());

        uploadService = new UploadService(parameterMap, PARAMETER_OPTIONS);

        return uploadService.executeUpload();
    }

    private ParameterMap populateParameters(ParameterMap params) throws CommandException {
        for(String opt : PARAMETER_OPTIONS) {
            params.add(opt, getOption(opt));
        }

        return params;
    }
    
}
