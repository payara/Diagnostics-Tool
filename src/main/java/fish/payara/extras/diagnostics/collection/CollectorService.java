package fish.payara.extras.diagnostics.collection;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import fish.payara.extras.diagnostics.collection.collectors.DomainXmlCollector;
import fish.payara.extras.diagnostics.collection.collectors.InstanceDomainXmlCollector;
import fish.payara.extras.diagnostics.collection.collectors.InstanceLogCollector;
import fish.payara.extras.diagnostics.collection.collectors.JVMCollector;
import fish.payara.extras.diagnostics.collection.collectors.LogCollector;
import fish.payara.extras.diagnostics.util.JvmCollectionType;
import fish.payara.extras.diagnostics.util.ParamConstants;
import org.glassfish.api.logging.LogLevel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CollectorService {
    Logger logger = Logger.getLogger(this.getClass().getName());

    private final Map<String, Collector> COLLECTORS;

    Map<String, String> parameterMap;
    String[] parameterOptions;

    public CollectorService(Map<String, String> params, String[] parameterOption) {
        this(params, parameterOption, null, null);
    }

    public CollectorService(Map<String, String> params, String[] parameterOptions, ProgramOptions programOptions, Environment environment) {

        this.parameterMap = params;
        this.parameterOptions = parameterOptions;
        COLLECTORS = new HashMap<>();
        COLLECTORS.put(ParamConstants.SERVER_LOG_PARAM, new LogCollector());
        COLLECTORS.put(ParamConstants.DOMAIN_XML_PARAM, new DomainXmlCollector());
        COLLECTORS.put(ParamConstants.INSTANCES_DOMAIN_XML_PARAM, new InstanceDomainXmlCollector());
        COLLECTORS.put(ParamConstants.INSTANCES_LOG_PARAM, new InstanceLogCollector());
        COLLECTORS.put(ParamConstants.DOMAIN_JVM_REPORT_PARAM, new JVMCollector(environment, programOptions));
        COLLECTORS.put(ParamConstants.INSTANCE_JVM_REPORT_PARAM, new JVMCollector(environment, programOptions, true));
        COLLECTORS.put(ParamConstants.DOMAIN_THREAD_DUMP_PARAM, new JVMCollector(environment, programOptions, JvmCollectionType.THREAD_DUMP));
        COLLECTORS.put(ParamConstants.INSTANCE_THREAD_DUMP_PARAM, new JVMCollector(environment, programOptions, true, JvmCollectionType.THREAD_DUMP));
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
        List<Collector> activeCollectors = getActiveCollectors(parameterMap, parameterOptions, COLLECTORS);

        int result = 0;
        if (activeCollectors.size() != 0) {
            for (Collector collector : activeCollectors) {
                collector.setParams(parameterMap);
                result = collector.collect();
                if (result != 0) {
                    return result;
                }
            }
        }

        File file = new File(parameterMap.get(ParamConstants.DIR_PARAM));

        try (FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath() + ".zip")) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            compressDirectory(file, file.getName(), zipOutputStream);
            zipOutputStream.close();

            cleanUpDirectory(file);
        } catch (IOException e) {
            logger.log(LogLevel.SEVERE, "Could not zip collected files.");
            return 1;
        }

        //Save output to properties, to make uplaod seamless for the user

        return result;
    }


    /**
     * Compresses the collected directory into a zip folder.
     *
     * @param file
     * @param fileName
     * @param zipOutputStream
     * @throws IOException
     */
    private void compressDirectory(File file, String fileName, ZipOutputStream zipOutputStream) throws IOException {
        if (file.isHidden()) {
            return;
        }

        if (file.isDirectory()) {

            zipOutputStream.putNextEntry(fileName.endsWith(File.separator) ? new ZipEntry(fileName) : new ZipEntry(fileName + File.separator));

            File[] children = file.listFiles();
            for (File childFile : children) {
                compressDirectory(childFile, fileName + File.separator + childFile.getName(), zipOutputStream);
            }
            return;
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInputStream.read(bytes)) >= 0) {
            zipOutputStream.write(bytes, 0, length);
        }

        fileInputStream.close();
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
                    .forEach(x -> deleteDirectory(x));
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
            logger.log(LogLevel.INFO, "Could not cleanup directory at {0}", path.toString());
        }
    }


    /**
     * Returns list of collectors which are enabled from user parameters.
     *
     * @param parameterMap
     * @param parameterOptions
     * @param collectors
     * @return List<Collector>
     */
    public List<Collector> getActiveCollectors(Map<String, String> parameterMap, String[] parameterOptions, Map<String, Collector> collectors) {
        List<Collector> activeCollectors = new ArrayList<>();

        if (parameterMap == null || parameterOptions == null || collectors == null) {
            return activeCollectors;
        }

        for (String parameter : parameterOptions) {
            String parameterValue = parameterMap.get(parameter);
            Boolean collectorValue = Boolean.valueOf(parameterValue);
            if (collectorValue) {
                activeCollectors.add(collectors.get(parameter));
            }
        }
        return activeCollectors;
    }
}
