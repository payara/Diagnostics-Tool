package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.util.ParamConstants;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class InstanceDomainXmlCollector extends FileCollector {

    Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    public int collect() {
        LOGGER.info("Collecting instance");

        Map<String, String> params = getParams();
        if (params == null) {
            return 0;
        }

        Path outputPath = Path.of(params.get(ParamConstants.DIR_PARAM));

        setDestination(outputPath);

        String instanceDomainPaths = params.get(ParamConstants.INSTANCES_DOMAIN_XML_PATH);

        instanceDomainPaths = instanceDomainPaths.replace("[", "");
        instanceDomainPaths = instanceDomainPaths.replace("]", "");
        List<String> paths = new ArrayList<>(Arrays.asList(instanceDomainPaths.split(",")));
        int result = 0;
        for(String path : paths){
            Path filePath = Path.of(path.strip());
            setFilePath(filePath);


            setInstanceName(String.valueOf(filePath.getParent().getParent().getFileName()));
            LOGGER.info("Instance name" + getInstanceName());
            result = super.collect();

        }
        return result;
    }
}
