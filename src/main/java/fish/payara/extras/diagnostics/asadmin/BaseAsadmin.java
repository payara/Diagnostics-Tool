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

public abstract class BaseAsadmin extends LocalDomainCommand {
    private static final String OUTPUT_DIR_PARAM_SYS_PROP = "fish.payara.diagnostics.path";

    protected static final String DIR_PARAM = "dir";
    protected static final String DIR_NAME = "/payara-diagnostics-" + System.currentTimeMillis();

    @Param(name = DIR_PARAM, shortName = "f", optional = true, defaultCalculator = DefaultOutputDirParam.class)
    protected String dir;

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

    public static class DefaultOutputDirParam extends ParamDefaultCalculator {
        private static final String JAV_DIR_SYS_PROP = "user.home";

        @Override
        public String defaultValue(ExecutionContext context) {
            return System.getProperty(OUTPUT_DIR_PARAM_SYS_PROP, System.getProperty(JAV_DIR_SYS_PROP));
        }
    }
}