package fish.payara.extras.diagnostics.collector;

import javax.inject.Inject;

import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

@Service(name = "diagnostics-tool")
@PerLookup
public class CollectAsadmin extends LocalDomainCommand {

    Logger logger = Logger.getLogger(getClass().getClass());
    
    @Inject
    private ServiceLocator habitat;

    @Override
    protected int executeCommand() throws CommandException {
        logger.log(Level.INFO, "Command Executed");
        return 0;
    }


}
