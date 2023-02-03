package fish.payara.extras.diagnostics.asadmin;

import java.util.Properties;

import javax.inject.Inject;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

import fish.payara.extras.diagnostics.collection.CollectorService;

@Service(name = "collect")
@PerLookup
public class CollectAsadmin extends LocalDomainCommand {

    Logger logger = Logger.getLogger(getClass().getClass());

    @Param(name = "serverlog", optional = true)
    private boolean collectServerLog;

    @Param(name = "accesslog", optional = true)
    private boolean collectAccessLog;

    @Override
    protected int executeCommand() throws CommandException {
        // TODO implementation
        return 0;
    }
}
