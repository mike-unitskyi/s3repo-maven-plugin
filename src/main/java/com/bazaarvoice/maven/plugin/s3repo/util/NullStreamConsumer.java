package com.bazaarvoice.maven.plugin.s3repo.util;

import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Simply ignores any data
 */
public final class NullStreamConsumer implements StreamConsumer {
    public static final NullStreamConsumer theInstance = new NullStreamConsumer();

    @Override
    public void consumeLine(String line) {
        // ignore
    }
}
