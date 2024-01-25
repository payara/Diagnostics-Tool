package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.util.ParamConstants;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InstanceLogCollector extends LogCollector {

    @Override
    public int collect() {
        Map<String, String> params = getParams();
        if (params == null) {
            return 0;
        }
        String instanceLogPaths = params.get(ParamConstants.INSTANCES_LOG_PATH);
        instanceLogPaths = instanceLogPaths.replace("[", "");
        instanceLogPaths = instanceLogPaths.replace("]", "");
        List<String> paths = new ArrayList<>(Arrays.asList(instanceLogPaths.split(",")));
        int result = 0;

        for (String path : paths) {
            if ("".equals(path)) {
                continue;
            }
            Path filePath = Path.of(path.strip());
            setLogPath(filePath);


            setInstanceName(String.valueOf(filePath.getParent().getFileName()));
            result = super.collect();

        }
        setInstanceName(null);
        return result;
    }

}
