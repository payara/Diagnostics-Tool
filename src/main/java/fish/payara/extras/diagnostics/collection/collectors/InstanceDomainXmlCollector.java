package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.util.ParamConstants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InstanceDomainXmlCollector extends FileCollector {

    @Override
    public int collect() {
        Map<String, String> params = getParams();
        if (params == null) {
            return 0;
        }

        Path outputPath = Paths.get(params.get(ParamConstants.DIR_PARAM));

        setDestination(outputPath);

        String instanceDomainPaths = params.get(ParamConstants.INSTANCES_DOMAIN_XML_PATH);

        instanceDomainPaths = instanceDomainPaths.replace("[", "");
        instanceDomainPaths = instanceDomainPaths.replace("]", "");
        List<String> paths = new ArrayList<>(Arrays.asList(instanceDomainPaths.split(",")));
        int result = 0;
        for(String path : paths){
            if ("".equals(path)) {
                continue;
            }
            Path filePath = Paths.get(path.trim());
            setFilePath(filePath);


            setInstanceName(String.valueOf(filePath.getParent().getParent().getFileName()));
            result = super.collect();

        }
        return result;
    }
}
