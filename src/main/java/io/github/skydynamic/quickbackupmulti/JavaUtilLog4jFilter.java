package io.github.skydynamic.quickbackupmulti;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class JavaUtilLog4jFilter extends AbstractFilter implements Filter {
    public boolean isLoggable(@NotNull LogRecord record) {
        return !QuickBackupMulti.shouldFilterMessage(
            Level.valueOf(record.getLevel().getName()), record.getLoggerName()
        );
    }

    public Result filter(@NotNull LogEvent event) {
        return QuickBackupMulti.shouldFilterMessage(
            event.getLevel(), event.getLoggerName()
        ) ? Result.DENY : Result.NEUTRAL;
    }
}
