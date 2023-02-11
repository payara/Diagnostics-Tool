package fish.payara.extras.diagnostics.collection.collectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.logging.LogLevel;

public class DomainXmlCollector extends FileCollector {
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public int collect() {
        ParameterMap params = getParams();
        if(params != null) {
            Path outputPath = getPathFromParams("outputDir", params);
            Path domainXmlPath = getPathFromParams("DomainXMLFilePath", params);
            if(domainXmlPath != null && outputPath != null) {
                System.out.println("DomainXML Path: " + domainXmlPath.toString());
                System.out.println("Output Path: " + outputPath.toString());
                try {
                    setFilePath(domainXmlPath);
                } catch(FileNotFoundException fnfe) {
                    logger.log(LogLevel.WARNING, "Domain XML could not be found at path: " + domainXmlPath);
                    return 0;
                }

                try {
                    //outputPath = outputPath.resolve("domain.xml");
                    System.out.println("Output Path 2: " + outputPath.toString());
                    setDestination(outputPath);
                } catch(FileNotFoundException fnfe) {
                    logger.log(LogLevel.WARNING, "Output directory could not be found or created at path: " + outputPath);
                    return 0;
                } catch (IOException io) {
                    io.printStackTrace();
                }
                
                return super.collect();
            }
        }

        return 1;
    }

    private Path getPathFromParams(String key, ParameterMap parameterMap) {
        ParameterMap params = parameterMap;
        if(params != null) {
            String valueString = params.getOne(key);
            if(valueString != null) {
                Path path = Path.of(valueString);
                return path;
            }
        }

        return null;
    }

    

}
