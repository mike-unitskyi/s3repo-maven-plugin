package com.bazaarvoice.maven.plugin.s3repo.util;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Simply logs the stream output as a warning
 */
public final class LogStreamConsumer implements StreamConsumer {

    private final Log _log;

    public LogStreamConsumer(Log log) {
        _log = log;
    }

    @Override
    public void consumeLine(String line) {
        _log.warn(line);
    }
}
