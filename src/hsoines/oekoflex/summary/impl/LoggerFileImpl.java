package hsoines.oekoflex.summary.impl;

import hsoines.oekoflex.summary.LoggerFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Loggt in Datei
 * Basiert auf log4j
 */
public final class LoggerFileImpl implements LoggerFile {
    private static final Log log = LogFactory.getLog(LoggerFileImpl.class);

    private final Logger logger;
    private final FileAppender appender;
    private final ScheduledExecutorService executorService;
    private final LinkedBlockingQueue<String> loggingEvents;

    public LoggerFileImpl(final String name, final String scenario) throws IOException {
        this(name, scenario, 100000);
    }

    public LoggerFileImpl(final String name, final String scenario, final int queueSize) throws IOException {
        String scenarioLogDir = scenario + "/";
        String loggerFilename = scenarioLogDir + name + ".log.csv";
        appender = new FileAppender(new SimpleLayout(), loggerFilename);
        logger = RootLogger.getLogger(name);
        appender.setLayout(new PatternLayout("%m"));
        loggingEvents = new LinkedBlockingQueue<>(queueSize);

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    String s;
                    while ((s = loggingEvents.take()) != null) {
                        final LoggingEvent event = buildEvent(s);
                        appender.doAppend(event);
                    }
                } catch (Throwable e) {
                    System.err.println(e.getMessage());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void log(String text) {
        try {
            loggingEvents.put(text);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        appender.close();
    }

    LoggingEvent buildEvent(final String s) {
        return new LoggingEvent("", logger, Level.toLevel("INFO"), s + System.getProperty("line.separator"), null);
    }
}
