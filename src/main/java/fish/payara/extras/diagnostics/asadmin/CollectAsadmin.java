package fish.payara.extras.diagnostics.asadmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.UnexpectedException;

import javax.inject.Inject;

import org.glassfish.api.ExecutionContext;
import org.glassfish.api.Param;
import org.glassfish.api.ParamDefaultCalculator;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.jboss.logging.Logger;
import org.jvnet.hk2.annotations.Service;

import fish.payara.extras.diagnostics.collection.CollectorService;

@Service(name = "collect")
@PerLookup
public class CollectAsadmin extends BaseAsadmin {

    Logger logger = Logger.getLogger(getClass().getClass());

    private static final String[] parameterOptions = {"serverLog", "accessLog", "domainXml"};

    @Param(name = "serverLog", shortName = "s", optional = true, defaultValue = "true")
    private boolean collectServerLog;

    @Param(name = "accessLog", shortName = "a", optional = true, defaultValue = "true")
    private boolean collectAccessLog;

    @Param(name = "domainXml", shortName = "d", optional = true, defaultValue = "true")
    private boolean collectDomainXml;

    private CollectorService collectorService;

    @Override
    protected int executeCommand() throws CommandException {
        ParameterMap parameterMap = new ParameterMap();

        for(String opt : parameterOptions) {
            parameterMap.add(opt, getOption(opt));
        }        
        parameterMap.add("DomainXMLFilePath", getDomainXml().getAbsolutePath());
        parameterMap.add("DomainName", getDomainName());

        parameterMap.add("java.util.logging.config.file", getLoggingConfigFilePath());
        collectorService = new CollectorService(parameterMap);

        return 0;
    }

    private String getLoggingConfigFilePath() {
        String loggingConfigFile = getSystemProperty("java.util.logging.config.file");
        if(loggingConfigFile != null) {
            return getSystemProperty("java.util.logging.config.file");
        }
        return "";
    }
}
