package fish.payara.extras.diagnostics.asadmin;

import org.glassfish.api.admin.ParameterMap;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

import fish.payara.extras.diagnostics.collection.CollectorService;

public abstract class BaseAsadmin extends LocalDomainCommand {

    protected CollectorService collectorService;

    protected ParameterMap parameterMap;
}