package fish.payara.extras.diagnostics.collection.collectors;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.trilead.ssh2.SCPClient;
import fish.payara.extras.diagnostics.collection.Collector;
import fish.payara.extras.diagnostics.collection.CollectorService;
import fish.payara.extras.diagnostics.util.ParamConstants;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeapDumpCollector implements Collector {

    private final String SSH = "SSH";

    private Map<String, Object> params;

    private String target;
    private ProgramOptions programOptions;
    private Logger LOGGER = Logger.getLogger(HeapDumpCollector.class.getName());
    private Environment environment;
    private boolean correctDomain;
    private String targetType;
    private Node node;
    private String nodeInstallationDirectory;
    private ServiceLocator serviceLocator;
    private String dirSuffix;

    public HeapDumpCollector(String target, ProgramOptions programOptions, Environment environment, String dirSuffix, CollectorService collectorService) {
        this.target = target;
        this.programOptions = programOptions;
        this.environment = environment;
        this.dirSuffix = dirSuffix;
        this.correctDomain = true;
        this.targetType = collectorService.returnInstanceType(target);
        this.node = collectorService.returnCurrentNode(target);
        this.serviceLocator = collectorService.returnServiceLocator();
        this.nodeInstallationDirectory = collectorService.returnNodeInstallationDirectory(target);
    }


    @Override
    public int collect() {
        LOGGER.info("Collecting Heap Dump from " + target);

        if (!correctDomain) {
            LOGGER.info("The targeted domain is not running!");
            return 0;
        }
        try {
            ParameterMap parameterMap = new ParameterMap();
            if (!target.equals("server")) {
                parameterMap.add("target", target);
            }
            String outputPathString = (String) params.get(ParamConstants.DIR_PARAM);
            Path outputPath = Paths.get(outputPathString, dirSuffix != null ? dirSuffix : "");
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            if (targetType.equals("CONFIG")) {
                //If it is local, uses destination folder as outputDir
                parameterMap.add("outputDir", outputPath.toString());
            }
            if (targetType.equals(SSH)) {
                parameterMap.add("outputDir", nodeInstallationDirectory);
            }
            programOptions.updateOptions(parameterMap);
            programOptions.setInteractive(false);

            RemoteCLICommand remoteCLICommand = new RemoteCLICommand("generate-heap-dump", programOptions, environment);
            String result = remoteCLICommand.executeAndReturnOutput();
            LOGGER.info(result);

            // Extract file name from command output
            String fileName = null;
            Pattern pattern = Pattern.compile("File name is (.+)");
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                fileName = matcher.group(1) + ".hprof";
                LOGGER.info("Extracted file name: " + fileName);
            } else {
                LOGGER.warning("Could not extract file name from result.");
            }

            if (targetType.equals(SSH)) {
                LOGGER.info("Downloading Heap Dump from remote host.");
                String remoteFile = nodeInstallationDirectory+"/"+fileName;
                downloadFileUsingSCP(remoteFile, outputPath.toString(), fileName);
            }
            if (result.startsWith("Warning:") && result.contains("seems to be offline; command generate-heap-dump was not replicated to that instance")) {
                LOGGER.warning(target + "is offline! Heap Dump will NOT be collected!");
            }
        } catch (CommandException e) {
            if (e.getMessage().contains("Remote server does not listen for requests on")) {
                LOGGER.warning("Server is offline! Heap Dump will not be collected!");
                return 0;
            }
            if (e.getMessage().contains("has never been started; command generate-heap-dump was not replicated to that instance")) {
                LOGGER.warning(target + " has never been started! Heap Dump will not be collected!");
                return 0;
            }

            if (e.getMessage().contains("Command generate-heap-dump not found.")) {
                LOGGER.warning("This version of Payara does not support heap dump generation.");
                return 0;
            }

            if (e.getMessage().contains("Unable to find a valid target with name")) {
                LOGGER.info(String.format("The domain containing %s is not running! Heap Dump will not be collected", target));
                return 0;
            }
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    private boolean downloadFileUsingSCP(String remoteFile, String localDirectory, String fileName) throws IOException {
        try  {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            String deleteFileCommand = "rm "+ nodeInstallationDirectory +"/" + fileName;
            SSHLauncher sshL = getSSHL(serviceLocator);
            sshL.init(node, LOGGER);
            SCPClient scpClient = sshL.getSCPClient();

            Path outputDir = Paths.get(localDirectory);
            Path localFile = outputDir.resolve(Paths.get(remoteFile).getFileName());

            scpClient.get(remoteFile, localDirectory);
            sshL.runCommand(deleteFileCommand,outStream);
            LOGGER.info("Heap dump file downloaded successfully to: " + localFile);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Error downloading heap dump file: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params = params;
        }
    }

    private SSHLauncher getSSHL(ServiceLocator habitat) {
        return habitat.getService(SSHLauncher.class);
    }
}
