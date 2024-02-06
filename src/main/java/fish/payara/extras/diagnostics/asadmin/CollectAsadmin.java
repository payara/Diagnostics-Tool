package fish.payara.extras.diagnostics.asadmin;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;
import fish.payara.extras.diagnostics.collection.CollectorService;
import fish.payara.extras.diagnostics.util.ParamConstants;
import fish.payara.extras.diagnostics.util.PropertiesFile;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fish.payara.extras.diagnostics.util.ParamConstants.INSTANCES_DOMAIN_XML_PATH;
import static fish.payara.extras.diagnostics.util.ParamConstants.INSTANCES_LOG_PATH;

@Service(name = "collect-diagnostics")
@PerLookup
public class CollectAsadmin extends BaseAsadmin {
    private static final String SERVER_LOG_PARAM = ParamConstants.SERVER_LOG_PARAM;
    private static final String DOMAIN_XML_PARAM = ParamConstants.DOMAIN_XML_PARAM;
    private static final String INSTANCES_DOMAIN_XML_PARAM = ParamConstants.INSTANCES_DOMAIN_XML_PARAM;
    private static final String INSTANCES_LOG_PARAM = ParamConstants.INSTANCES_LOG_PARAM;
    private static final String DOMAIN_JVM_REPORT_PARAM = ParamConstants.DOMAIN_JVM_REPORT_PARAM;
    private static final String INSTANCE_JVM_REPORT_PARAM = ParamConstants.INSTANCE_JVM_REPORT_PARAM;
    private static final String DOMAIN_THREAD_DUMP_PARAM = ParamConstants.DOMAIN_THREAD_DUMP_PARAM;
    private static final String INSTANCE_THREAD_DUMP_PARAM = ParamConstants.INSTANCE_THREAD_DUMP_PARAM;
    private static final String DOMAIN_NAME_PARAM = ParamConstants.DOMAIN_NAME_PARAM;
    private static final String[] PARAMETER_OPTIONS = {SERVER_LOG_PARAM, DOMAIN_XML_PARAM, INSTANCES_DOMAIN_XML_PARAM, INSTANCES_LOG_PARAM, DOMAIN_JVM_REPORT_PARAM, INSTANCE_JVM_REPORT_PARAM, DOMAIN_THREAD_DUMP_PARAM, INSTANCE_THREAD_DUMP_PARAM, DIR_PARAM};
    private static final String DOMAIN_NAME = ParamConstants.DOMAIN_NAME;
    private static final String DOMAIN_XML_FILE_PATH = ParamConstants.DOMAIN_XML_FILE_PATH;
    private static final String LOGS_PATH = ParamConstants.LOGS_PATH;
    private static final String INSTANCES_NAMES = ParamConstants.INSTANCES_NAMES;
    Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Param(name = SERVER_LOG_PARAM, shortName = "s", optional = true, defaultValue = "true")
    private boolean collectServerLog;

    @Param(name = DOMAIN_XML_PARAM, shortName = "d", optional = true, defaultValue = "true")
    private boolean collectDomainXml;

    @Param(name = INSTANCES_DOMAIN_XML_PARAM, optional = true, defaultValue = "true")
    private boolean collectInstanceDomainXml;

    @Param(name = INSTANCES_LOG_PARAM, optional = true, defaultValue = "true")
    private boolean collectInstanceLog;

    @Param(name = DOMAIN_JVM_REPORT_PARAM, optional = true, defaultValue = "true")
    private boolean collectDomainJvmReport;

    @Param(name = INSTANCE_JVM_REPORT_PARAM, optional = true, defaultValue = "true")
    private boolean collectInstanceJvmReport;

    @Param(name = DOMAIN_THREAD_DUMP_PARAM, optional = true, defaultValue = "true")
    private boolean collectDomainThreadDump;

    @Param(name = INSTANCE_THREAD_DUMP_PARAM, optional = true, defaultValue = "true")
    private boolean collectInstanceThreadDump;

    @Param(name = DOMAIN_NAME_PARAM, optional = true, primary = true, defaultValue = "domain1")
    private String domainName;

    private CollectorService collectorService;

    @Inject
    ServiceLocator serviceLocator;
    
    /** 
     * Execute asadmin command Collect.
     * 
     * 0 - success
     * 1 - failure
     * 
     * @return int
     * @throws CommandException
     */
    @Override
    protected int executeCommand() throws CommandException {
        setDomainName(domainName);
        initDomain();
        parameterMap = populateParameters(new HashMap<String, String>(), PARAMETER_OPTIONS);
        parameterMap = resolveDir(parameterMap);

        collectorService = new CollectorService(parameterMap, PARAMETER_OPTIONS, programOpts, env);
        PropertiesFile props = getProperties();
        props.store(DIR_PARAM, parameterMap.get(DIR_PARAM));
        return collectorService.executeCollection();
    }

    /** 
     * Populates parameters with Parameter options into a map. Overriden method add some more additionaly properties required by the collect command.
     * 
     * @param params
     * @param paramOptions0
     * @return Map<String, String>
     */
    @Override
    protected Map<String, String> populateParameters(Map<String, String> params, String[] paramOptions) {
        params = super.populateParameters(params, paramOptions);

        params.put(DOMAIN_XML_FILE_PATH, getDomainXml().getAbsolutePath());
        params.put(DOMAIN_NAME, domainName);
        params.put(INSTANCES_DOMAIN_XML_PATH, getInstancePaths(PathType.DOMAIN));
        params.put(INSTANCES_LOG_PATH, getInstancePaths(PathType.LOG));
        params.put(LOGS_PATH, getDomainRootDir().getPath() + "/logs");
        params.put(INSTANCES_NAMES, getInstancesNames());

        return params;
    }

    private DomDocument getDocument() {
        File domainXmlFile = Paths.get(getDomainXml().getAbsolutePath()).toFile();
        ConfigParser configParser = new ConfigParser(serviceLocator);

        try {
            configParser.logUnrecognisedElements(false);
        } catch (NoSuchMethodError noSuchMethodError) {
            LOGGER.log(Level.FINE, "Using a version of ConfigParser that does not support disabling log messages via method",
                    noSuchMethodError);
        }

        URL domainUrl;
        try {
            domainUrl = domainXmlFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        DomDocument doc = configParser.parse(domainUrl);
        return doc;
    }

    private Map<String, Path> getNodePaths(){
        DomDocument doc = getDocument();
        Map<String, Path> nodePaths = new HashMap<>();
        for (Node node : doc.getRoot().createProxy(Domain.class).getNodes().getNode()) {
            if (!node.getType().equals("CONFIG")) {
                continue;
            }

            if(!node.isLocal()){
                continue;
            }
            nodePaths.put(node.getName(), Paths.get(node.getInstallDir().replace("${com.sun.aas.productRoot}", System.getProperty("com.sun.aas.productRoot")), "glassfish", "nodes", node.getName()));
        }
        return nodePaths;
    }

    private Map<String, List<String>> getServersInNodes() {
        DomDocument doc = getDocument();
        Map<String, List<String>> nodesAndServers = new HashMap<>();
        for (Server server : doc.getRoot().createProxy(Domain.class).getServers().getServer()) {
            if (server.getConfig().isDas()) {
                continue;
            }

            if (!nodesAndServers.containsKey(server.getNodeRef())) {
                List<String> servers = new ArrayList<>();
                servers.add(server.getName());
                nodesAndServers.put(server.getNodeRef(), servers);
                continue;
            }
            List<String> servers = nodesAndServers.get(server.getNodeRef());
            servers.add(server.getName());
            nodesAndServers.put(server.getNodeRef(), servers);
        }
        return nodesAndServers;
    }
    private String getInstancePaths(PathType pathType) {
        Map<String, Path> nodePaths = getNodePaths();
        Map<String, List<String>> nodesAndServers = getServersInNodes();
        List<Path> instanceXmlPaths = new ArrayList<>();
        for (String nodeName : nodePaths.keySet()) {
            List<String> instances = nodesAndServers.get(nodeName);
            if (instances == null) {
                continue;
            }
            if (pathType == PathType.DOMAIN) {
                instances.forEach(s -> instanceXmlPaths.add(Paths.get(String.valueOf(nodePaths.get(nodeName)),s,"config","domain.xml")));
                continue;
            }

            if(pathType == PathType.LOG) {
                instances.forEach(s -> instanceXmlPaths.add(Paths.get(String.valueOf(nodePaths.get(nodeName)),s,"logs")));
            }
        }
        return instanceXmlPaths.toString();
    }

    private String getInstancesNames() {
        Map<String, Path> nodePaths = getNodePaths();
        Map<String, List<String>> nodesAndServers = getServersInNodes();
        List<String> instanceList = new ArrayList<>();

        nodePaths.keySet().forEach(s -> {
            if (nodesAndServers.get(s) == null) {
                return;
            }
            instanceList.addAll(nodesAndServers.get(s));

        });

        return instanceList.toString();
    }

    enum PathType {
        DOMAIN, LOG;
    }
}
