package fish.payara.extras.diagnostics.asadmin;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.api.ExecutionContext;
import org.glassfish.api.Param;
import org.glassfish.api.ParamDefaultCalculator;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;

import fish.payara.extras.diagnostics.util.ParamConstants;
import fish.payara.extras.diagnostics.util.PropertiesFile;

public abstract class BaseAsadmin extends LocalDomainCommand {
    private static final String OUTPUT_DIR_PARAM_SYS_PROP = "fish.payara.diagnostics.path";
    private static final String PROPERTIES_PATH_SYS_PROP = "fish.payara.properties.path";
    private static final String JAV_DIR_SYS_PROP = "user.home";

    protected static final String DIR_PARAM = ParamConstants.DIR_PARAM;
    protected static final String DIR_NAME = "/payara-diagnostics-" + System.currentTimeMillis();

    private static final String PROPERTIES_PARAM = ParamConstants.PROPERTIES_PARAM;
    private static final String PROPERTIES_FILE_NAME = "." + PROPERTIES_PARAM;

    @Param(name = DIR_PARAM, shortName = "f", optional = true, defaultCalculator = DefaultOutputDirCalculator.class)
    protected String dir;

    @Param(name = PROPERTIES_PARAM, shortName = "p", optional = true, defaultCalculator = DefaultPropertiesPathCalculator.class)
    protected String propertiesPath;

    protected Map<String, String> parameterMap;

    protected Map<String, String> resolveDir(Map<String, String> params) {
        if(params == null) {
            return params;
        }

        if(dir != null) {
            if(Files.isDirectory(Path.of(dir))) {
                dir = dir + DIR_NAME;
            }
            params.put(DIR_PARAM, dir);
        }

        logger.log(Level.INFO, "Directory selected {0} ", dir);

        return params;
    }

    protected Map<String, String> populateParameters(Map<String, String> params, String[] paramOptions) {
        for(String opt : paramOptions) {
            params.put(opt, getOption(opt));
        }

        return params;
    }

    protected PropertiesFile getProperties() {
        if(propertiesPath != null) {
            Path path = Path.of(propertiesPath);
            return new PropertiesFile(path);
        }
        return null;
    }

    public static class DefaultOutputDirCalculator extends ParamDefaultCalculator {
        @Override
        public String defaultValue(ExecutionContext context) {
            return System.getProperty(OUTPUT_DIR_PARAM_SYS_PROP, System.getProperty(JAV_DIR_SYS_PROP));
        }
    }

    public static class DefaultPropertiesPathCalculator extends ParamDefaultCalculator {
        @Override
        public String defaultValue(ExecutionContext context) {
            return System.getProperty(PROPERTIES_PATH_SYS_PROP, System.getProperty(JAV_DIR_SYS_PROP) + "/" + PROPERTIES_FILE_NAME);
        }
    }
}