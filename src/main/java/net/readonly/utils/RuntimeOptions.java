package net.readonly.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RuntimeOptions {
    public static final boolean DEBUG = getValue("RO_DEBUG") != null;
    public static final boolean DEBUG_LOGS = getValue("RO_DEBUG_LOGS") != null;
    public static final boolean LOG_DB_ACCESS = getValue("RO_LOG_DB_ACCESS") != null;
    public static final boolean TRACE_LOGS = getValue("RO_TRACE_LOGS") != null;
    public static final boolean VERBOSE = getValue("RO_VERBOSE") != null;
    public static final boolean PRINT_VARIABLES = getValue("RO_PRINT_OPTIONS") != null;
    
    @Nullable
    private static String getValue(@Nonnull String name) {
        return System.getProperty(name, System.getenv(name));
    }
}
