package fish.payara.extras.diagnostics.collection.collectors;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import fish.payara.extras.diagnostics.collection.Collector;
import fish.payara.extras.diagnostics.util.JvmCollectionType;
import fish.payara.extras.diagnostics.util.ParamConstants;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class JVMCollector implements Collector {

    private Map<String, String> params;
    private final Environment environment;
    private final ProgramOptions programOptions;

    private final boolean collectInstances;
    private JvmCollectionType jvmCollectionType;

    public JVMCollector(Environment environment, ProgramOptions programOptions) {
        this(environment, programOptions, false);
    }

    public JVMCollector(Environment environment, ProgramOptions programOptions, JvmCollectionType jvmCollectionType) {
        this(environment, programOptions, false, jvmCollectionType);
    }

    public JVMCollector(Environment environment, ProgramOptions programOptions, Boolean collectInstances) {
        this(environment, programOptions, collectInstances, JvmCollectionType.JVM_REPORT);
    }

    public JVMCollector(Environment environment, ProgramOptions programOptions, Boolean collectInstances, JvmCollectionType jvmCollectionType) {
        this.environment = environment;
        this.programOptions = programOptions;
        this.collectInstances = collectInstances;
        this.jvmCollectionType = jvmCollectionType;
    }

    @Override
    public int collect() {
        if (collectInstances) {
            AtomicBoolean result = new AtomicBoolean(true);
            List<String> instancesList = getInstanceList();

            instancesList.forEach(instance -> {
                if ("".equals(instance)) {
                    return;
                }
                result.set(collectReport(instance.trim()));
            });

            if (result.get()) {
                return 0;
            }
            return 1;
        }
        if (collectReport("server")) {
            return 0;
        }
        return 1;
    }

    private List<String> getInstanceList() {
        String instances = params.get(ParamConstants.INSTANCES_NAMES);
        instances = instances.replace("[", "");
        instances = instances.replace("]", "");
        return new ArrayList<>(Arrays.asList(instances.split(",")));
    }

    private boolean writeToFile(String text, String fileName) {
        String outputPathString = params.get(ParamConstants.DIR_PARAM);
        Path outputPath = Paths.get(outputPathString);
        String suffix = jvmCollectionType == JvmCollectionType.JVM_REPORT ? "-jvm-report.txt" : "-thread-dump.txt";
        byte[] textBytes = text.getBytes();
        try {
            Files.write(Paths.get(outputPath + "/" + fileName + suffix), textBytes);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, String> params) {
        if (params != null) {
            this.params = params;
        }
    }

    private boolean collectReport(String target) {
        try {
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.add("target", target);
            parameterMap.add("type", jvmCollectionType.value);
            programOptions.updateOptions(parameterMap);
            RemoteCLICommand remoteCLICommand = new RemoteCLICommand("generate-jvm-report", programOptions, environment);
            return writeToFile(remoteCLICommand.executeAndReturnOutput(), target);
        } catch (CommandException e) {
            if (e.getMessage().contains("Remote server does not listen for requests on")) {
                System.out.println("Server offline! JVM Report will NOT be collected.");
                return true;
            }

            if (e.getMessage().contains("has never been started")) {
                System.out.println(target + " has not been started - cannot collect JVM Report");
                return true;
            }
            throw new RuntimeException(e);
        }
    }
}
