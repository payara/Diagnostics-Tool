/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2023-2024 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fish.payara.extras.diagnostics.collection;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.SystemPropertyConstants;
import fish.payara.enterprise.config.serverbeans.DeploymentGroup;
import fish.payara.extras.diagnostics.collection.collectors.DomainXmlCollector;
import fish.payara.extras.diagnostics.collection.collectors.HeapDumpCollector;
import fish.payara.extras.diagnostics.collection.collectors.JVMCollector;
import fish.payara.extras.diagnostics.collection.collectors.LogCollector;
import fish.payara.extras.diagnostics.util.DomainUtil;
import fish.payara.extras.diagnostics.util.JvmCollectionType;
import fish.payara.extras.diagnostics.util.TargetType;
import org.glassfish.api.logging.LogLevel;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fish.payara.extras.diagnostics.util.ParamConstants.*;

public class CollectorService {
    private static final Logger LOGGER = Logger.getLogger(CollectorService.class.getName());
    private final String target;
    private final Environment environment;
    private final ProgramOptions programOptions;
    private Boolean domainXml;
    private Boolean serverLog;
    private Boolean threadDump;
    private Boolean jvmReport;
    private Boolean heapDump;
    private Domain domain;
    private DomainUtil domainUtil;
    private final ServiceLocator serviceLocator;

    private final Map<String, Object> parameterMap;

    public CollectorService(Map<String, Object> params, Environment environment, ProgramOptions programOptions, String target, ServiceLocator serviceLocator) {
        this.parameterMap = params;
        this.target = target;
        this.environment = environment;
        this.programOptions = programOptions;
        this.serviceLocator = serviceLocator;
        init();
    }

    private void init() {
        domainXml = true;
        serverLog = true;
        threadDump = true;
        jvmReport = true;
        heapDump = true;

        if (parameterMap != null) {
            domainXml = parameterMap.get(DOMAIN_XML_PARAM) == null || Boolean.parseBoolean((String) parameterMap.get(DOMAIN_XML_PARAM));
            serverLog = parameterMap.get(SERVER_LOG_PARAM) == null || Boolean.parseBoolean((String) parameterMap.get(SERVER_LOG_PARAM));
            threadDump = parameterMap.get(THREAD_DUMP_PARAM) == null || Boolean.parseBoolean((String) parameterMap.get(THREAD_DUMP_PARAM));
            jvmReport = parameterMap.get(JVM_REPORT_PARAM) == null || Boolean.parseBoolean((String) parameterMap.get(JVM_REPORT_PARAM));
            heapDump = parameterMap.get(HEAP_DUMP_PARAM) == null || Boolean.parseBoolean((String) parameterMap.get(HEAP_DUMP_PARAM));
        }
    }


    /**
     * Executes collection of all specified collectors.
     * <p>
     * 0 - Success
     * 1 - Failure
     *
     * @return int
     */
    public int executeCollection() {
        if (parameterMap == null) {
            return 1;
        }

        if (parameterMap.containsKey(DOMAIN_XML_FILE_PATH)) {
            domain = getDomain((String) parameterMap.get(DOMAIN_XML_FILE_PATH));
        }

        List<Collector> activeCollectors;

        if (domain == null) {
            if (target.equals("domain")) {
                LOGGER.info("No domain found! Nothing will be collected");
                return 1;
            }
            String instanceRoot;
            File asadmin = new File(SystemPropertyConstants.getAsAdminScriptLocation());
            List<String> command = new ArrayList<>();
            command.add(asadmin.getAbsolutePath());
            command.add("--interactive=false");
            command.add("_get-instance-install-dir");
            command.add(target);

            ProcessManager processManager = new ProcessManager(command);
            processManager.waitForReaderThreads(true);
            try {
                processManager.setEcho(false);
                processManager.execute();
                if (processManager.getExitValue() == 0) {
                    String result = processManager.getStdout();
                    instanceRoot = result.replace("Command _get-instance-install-dir executed successfully.", "").trim();
                } else {
                    LOGGER.info("Target not found!");
                    return 1;
                }
            } catch (ProcessManagerException e) {
                throw new RuntimeException(e);
            }
            activeCollectors = getLocalCollectors(instanceRoot);

        } else {
            domainUtil = new DomainUtil(domain);
            activeCollectors = getActiveCollectors(parameterMap, getTargetType());
        }


        if (activeCollectors.isEmpty()) {
            LOGGER.info("No collectors are active. Nothing will be collected!");
            return 0;
        }

        int result = 0;
        for (Collector collector : activeCollectors) {
            collector.setParams(parameterMap);
            result = collector.collect();
            if (result != 0) {
                return result;
            }
        }

        Path filePath = Paths.get((String) parameterMap.get(DIR_PARAM));

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile() + ".zip")) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            compressDirectory(filePath, zipOutputStream);
            zipOutputStream.close();

            cleanUpDirectory(filePath.toFile());
        } catch (IOException e) {
            LOGGER.log(LogLevel.SEVERE, "Could not zip collected files.");
            return 1;
        }
        //Save output to properties, to make uplaod seamless for the user

        return result;
    }

    public TargetType getTargetType() {
        if (target.equals("domain")) {
            return TargetType.DOMAIN;
        }
        if (domainUtil.getInstancesNames().contains(target)) {
            return TargetType.INSTANCE;
        }

        if (domainUtil.getDeploymentGroups().getDeploymentGroup(target) != null) {
            return TargetType.DEPLOYMENT_GROUP;
        }

        if (domainUtil.getClusters().getCluster(target) != null) {
            return TargetType.CLUSTER;
        }
        return null;
    }


    /**
     * Compresses the collected directory into a zip folder.
     *
     * @param filePath
     * @param zipOutputStream
     * @throws IOException
     */
    private void compressDirectory(Path filePath, ZipOutputStream zipOutputStream) throws IOException {
        Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                zipOutputStream.putNextEntry(new ZipEntry(filePath.relativize(file).toString()));
                Files.copy(file, zipOutputStream);
                zipOutputStream.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * Removes specified directory.
     *
     * @param dir
     * @throws IOException
     */
    private void cleanUpDirectory(File dir) throws IOException {
        Path path = dir.toPath();
        try (Stream<Path> walk = Files.walk(path)) {
            walk
                    .sorted(Comparator.reverseOrder())
                    .forEach(this::deleteDirectory);
        }
    }


    /**
     * Utility method for directory cleanup. Removes files at specified path.
     *
     * @param path
     */
    private void deleteDirectory(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            LOGGER.log(LogLevel.INFO, "Could not cleanup directory at {0}", path.toString());
        }
    }


    /**
     * Returns list of collectors which are enabled from user parameters.
     *
     * @param parameterMap
     * @return List&lt;Collector&gt;
     */
    public List<Collector> getActiveCollectors(Map<String, Object> parameterMap, TargetType targetType) {
        List<Collector> activeCollectors = new ArrayList<>();

        if (parameterMap == null) {
            return activeCollectors;
        }
        if (targetType == TargetType.DOMAIN) {

            if (domainXml) {
                Path domainXmlPath = Paths.get((String) parameterMap.get(DOMAIN_XML_FILE_PATH));
                activeCollectors.add(new DomainXmlCollector(domainXmlPath));
            }
            if (serverLog) {
                Path domainLogPath = Paths.get((String) parameterMap.get(LOGS_PATH));
                activeCollectors.add(new LogCollector(domainLogPath));
            }

            if (jvmReport) {
                activeCollectors.add(new JVMCollector(environment, programOptions, "server", JvmCollectionType.JVM_REPORT));
            }

            if (threadDump) {
                activeCollectors.add(new JVMCollector(environment, programOptions, "server", JvmCollectionType.THREAD_DUMP));
            }

            if (heapDump) {
                activeCollectors.add(new HeapDumpCollector("server", programOptions, environment));
            }

            addInstanceCollectors(activeCollectors, domainUtil.getStandaloneLocalInstances(), "");

            for (DeploymentGroup deploymentGroup : domainUtil.getDeploymentGroups().getDeploymentGroup()) {
                addInstanceCollectors(activeCollectors, deploymentGroup.getInstances(), deploymentGroup.getName());
            }

            for (Cluster cluster : domainUtil.getClusters().getCluster()) {
                addInstanceCollectors(activeCollectors, cluster.getInstances(), cluster.getName());
            }

        }

        if (targetType == TargetType.INSTANCE) {
            List<Server> servers = new ArrayList<>();
            servers.add(domainUtil.getInstance(target));
            addInstanceCollectors(activeCollectors, servers, "");
        }

        if (targetType == TargetType.DEPLOYMENT_GROUP) {
            for (DeploymentGroup deploymentGroup : domainUtil.getDeploymentGroups().getDeploymentGroup()) {
                if (deploymentGroup.getName().equals(target)) {
                    addInstanceCollectors(activeCollectors, deploymentGroup.getInstances(), deploymentGroup.getName());
                }
            }
        }

        if (targetType == TargetType.CLUSTER) {
            for (Cluster cluster : domainUtil.getClusters().getCluster()) {
                if (cluster.getName().equals(target)) {
                    addInstanceCollectors(activeCollectors, cluster.getInstances(), cluster.getName());
                }
            }
        }
        return activeCollectors;
    }

    private List<Collector> getLocalCollectors(String instanceRoot) {
        List<Collector> activeCollectors = new ArrayList<>();
        if (domainXml) {
            activeCollectors.add(new DomainXmlCollector(Paths.get(instanceRoot, "config", "domain.xml"), target, null));
        }
        if (serverLog) {
            activeCollectors.add(new LogCollector(Paths.get(instanceRoot, "logs"), target));
        }

        if (jvmReport) {
            activeCollectors.add(new JVMCollector(environment, programOptions, target, JvmCollectionType.JVM_REPORT));
        }

        if (threadDump) {
            activeCollectors.add(new JVMCollector(environment, programOptions, target, JvmCollectionType.THREAD_DUMP));
        }
        if (heapDump) {
            activeCollectors.add(new HeapDumpCollector(target, programOptions, environment));
        }
        return activeCollectors;
    }

    private void addInstanceCollectors(List<Collector> activeCollectors, List<Server> serversList, String dirSuffix) {
        for (Server server : serversList) {
            String finalDirSuffix = Paths.get(dirSuffix, server.getName()).toString();
            if (domainXml) {
                activeCollectors.add(new DomainXmlCollector(Paths.get(domainUtil.getNodePaths().get(server.getNodeRef()).toString(), server.getName(), "config", "domain.xml"), server.getName(), finalDirSuffix));
            }
            if (serverLog) {
                activeCollectors.add(new LogCollector(Paths.get(domainUtil.getNodePaths().get(server.getNodeRef()).toString(), server.getName(), "logs"), server.getName(), finalDirSuffix));
            }

            if (jvmReport) {
                activeCollectors.add(new JVMCollector(environment, programOptions, server.getName(), JvmCollectionType.JVM_REPORT, finalDirSuffix));
            }

            if (threadDump) {
                activeCollectors.add(new JVMCollector(environment, programOptions, server.getName(), JvmCollectionType.THREAD_DUMP, finalDirSuffix));
            }
            if (heapDump) {
                activeCollectors.add(new HeapDumpCollector(server.getName(), programOptions, environment, finalDirSuffix));
            }
        }
    }

    private Domain getDomain(String domainXmlPath) {
        File domainXml = Paths.get(domainXmlPath).toFile();
        ConfigParser configParser = new ConfigParser(serviceLocator);

        try {
            configParser.logUnrecognisedElements(false);
        } catch (NoSuchMethodError noSuchMethodError) {
            LOGGER.log(Level.FINE, "Using a version of ConfigParser that does not support disabling log messages via method",
                    noSuchMethodError);
        }

        URL domainUrl;
        try {
            domainUrl = domainXml.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return configParser.parse(domainUrl).getRoot().createProxy(Domain.class);
    }
}
