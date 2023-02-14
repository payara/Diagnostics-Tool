package fish.payara.extras.diagnostics.asadmin;

import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name = "collect")
@PerLookup
public class UploadAsadmin extends BaseAsadmin {

    private static final String[] PARAMETER_OPTIONS = {"serverLogs", "domainXml", "outputDir"};

    @Override
    protected int executeCommand() throws CommandException {
        
        return 0;
    }
    
}
