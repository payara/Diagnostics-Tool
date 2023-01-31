package fish.payara.extras.diagnostics.collector;

import org.glassfish.api.admin.CommandException;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

@Service(name = "diagnostics-tool")
public class CollectAsadmin extends LocalDomainCommand {
    
    @Override
    protected int executeCommand() throws CommandException {
        System.out.println("Hello World");
        return 0;
    }
}
