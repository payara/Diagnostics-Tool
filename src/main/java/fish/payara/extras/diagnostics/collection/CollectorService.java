package fish.payara.extras.diagnostics.collection;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fish.payara.extras.diagnostics.collection.collectors.InstanceDomainXmlCollector;
import org.glassfish.api.logging.LogLevel;

import static java.util.Map.entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import fish.payara.extras.diagnostics.collection.collectors.DomainXmlCollector;
import fish.payara.extras.diagnostics.collection.collectors.LogCollector;
import fish.payara.extras.diagnostics.util.ParamConstants;

public class CollectorService {
    Logger logger = Logger.getLogger(this.getClass().getName());
    
    private static final Map<String, Collector> COLLECTORS = Map.ofEntries(
        entry(ParamConstants.SERVER_LOG_PARAM, new LogCollector()),
        entry(ParamConstants.DOMAIN_XML_PARAM, new DomainXmlCollector()),
        entry(ParamConstants.INSTANCES_DOMAIN_XML_PARAM, new InstanceDomainXmlCollector())
    );

    Map<String, String> parameterMap;
    String[] parameterOptions;

    public CollectorService(Map<String, String> params, String[] parameterOptions) {
        this.parameterMap = params;
        this.parameterOptions = parameterOptions;
    }

    
    /** 
     * Executes collection of all specified collectors.
     * 
     * 0 - Success
     * 1 - Failure
     * 
     * @return int
     */
    public int executeCollection() {
        List<Collector> activeCollectors = getActiveCollectors(parameterMap, parameterOptions, COLLECTORS);

        int result = 0;
        if(activeCollectors.size() != 0) {
            for(Collector collector : activeCollectors) {
                collector.setParams(parameterMap);
                result = collector.collect();
                if(result != 0) {
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
        if(file.isHidden()) {
            return;
        }

        if(file.isDirectory()) {

            zipOutputStream.putNextEntry(fileName.endsWith(File.separator) ? new ZipEntry(fileName) : new ZipEntry(fileName + File.separator));

            File[] children = file.listFiles();
            for(File childFile : children) {
                compressDirectory(childFile, fileName + File.separator + childFile.getName(), zipOutputStream);
            }
            return;
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while((length = fileInputStream.read(bytes)) >= 0) {
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
        try(Stream<Path> walk = Files.walk(path)) {
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
        } catch(IOException e) {
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

        if(parameterMap == null || parameterOptions == null || collectors == null) {
            return activeCollectors;
        }

        for (String parameter : parameterOptions) {
            String parameterValue = parameterMap.get(parameter);
            Boolean collectorValue = Boolean.valueOf(parameterValue);
            if(collectorValue) {
                activeCollectors.add(collectors.get(parameter));
            }
        }
        return activeCollectors;
    }

}
