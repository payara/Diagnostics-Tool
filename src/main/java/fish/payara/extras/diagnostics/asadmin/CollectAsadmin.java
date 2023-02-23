package fish.payara.extras.diagnostics.asadmin;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import fish.payara.extras.diagnostics.collection.CollectorService;
import fish.payara.extras.diagnostics.util.PropertiesFile;

@Service(name = "collect")
@PerLookup
public class CollectAsadmin extends BaseAsadmin {
    private static final String SERVER_LOG_PARAM = "serverLogs";
    private static final String DOMAIN_XML_PARAM = "domainXml";

    private static final String[] PARAMETER_OPTIONS = {SERVER_LOG_PARAM, DOMAIN_XML_PARAM, DIR_PARAM};

    private static final String DOMAIN_NAME = "DomainName";
    private static final String DOMAIN_XML_FILE_PATH = "DomainXMLFilePath";
    private static final String LOGS_PATH = "LogPath";

    @Param(name = SERVER_LOG_PARAM, shortName = "s", optional = true, defaultValue = "true")
    private boolean collectServerLog;

    @Param(name = DOMAIN_XML_PARAM, shortName = "d", optional = true, defaultValue = "true")
    private boolean collectDomainXml;

    private CollectorService collectorService;

    @Override
    protected int executeCommand() throws CommandException {
        parameterMap = populateParameters(new HashMap<String, String>(), PARAMETER_OPTIONS);
        parameterMap = resolveDir(parameterMap);

        collectorService = new CollectorService(parameterMap, PARAMETER_OPTIONS);

        PropertiesFile props = getProperties();
        props.store(DIR_PARAM, parameterMap.get(DIR_PARAM));

        return collectorService.executCollection();
    }

    @Override
    protected Map<String, String> populateParameters(Map<String, String> params, String[] paramOptions) {
        for(String opt : paramOptions) {
            params.put(opt, getOption(opt));
        }

        params.put(DOMAIN_XML_FILE_PATH, getDomainXml().getAbsolutePath());
        params.put(DOMAIN_NAME, getDomainName());

        params.put(LOGS_PATH, getDomainRootDir().getPath() + "/logs");

        return params;
    }
}
