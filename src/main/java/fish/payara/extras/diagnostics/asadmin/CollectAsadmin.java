package fish.payara.extras.diagnostics.asadmin;

import java.util.Properties;

import javax.inject.Inject;

import org.glassfish.api.admin.CommandException;
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

    @Override
    protected int executeCommand() throws CommandException {
        Properties props = new Properties();
        props.setProperty("Key", "Value");
        CollectorService service = new CollectorService(props);
        System.out.println(service.Collect());
        logger.log(Level.INFO, "Command Executed");
        return 0;
    }
}
