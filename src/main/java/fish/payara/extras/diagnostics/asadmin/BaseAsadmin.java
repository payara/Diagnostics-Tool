package fish.payara.extras.diagnostics.asadmin;

import javax.inject.Inject;

import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

public abstract class BaseAsadmin extends LocalDomainCommand {
    @Inject
    private ServiceLocator habitat;
}
