package net.readonly.logs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import net.readonly.utils.RuntimeOptions;

public class LogFilter extends Filter<ILoggingEvent> {
    public static final Level LEVEL = RuntimeOptions.TRACE_LOGS ? Level.TRACE : RuntimeOptions.DEBUG_LOGS ? Level.DEBUG : Level.INFO;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if (event.getLevel().isGreaterOrEqual(LEVEL)) {
            return FilterReply.NEUTRAL;
        } else {
            return FilterReply.DENY;
        }
    }
}
