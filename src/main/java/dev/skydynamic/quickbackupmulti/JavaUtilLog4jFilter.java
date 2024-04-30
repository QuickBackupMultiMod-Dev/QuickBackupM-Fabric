package dev.skydynamic.quickbackupmulti;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class JavaUtilLog4jFilter extends AbstractFilter implements Filter {
    public boolean isLoggable(@NotNull LogRecord record) {
        return !QuickBackupMulti.shouldFilterMessage(record.getMessage());
    }

    public Result filter(@NotNull LogEvent event) {
        return QuickBackupMulti.shouldFilterMessage("[" + event.getLoggerName() + "]: " + event.getMessage().getFormattedMessage()) ? Result.DENY : Result.NEUTRAL;
    }
}
