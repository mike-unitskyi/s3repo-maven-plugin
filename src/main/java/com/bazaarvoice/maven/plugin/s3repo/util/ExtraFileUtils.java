package com.bazaarvoice.maven.plugin.s3repo.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

public final class ExtraFileUtils {

    private ExtraFileUtils() {}

    public static void createOrCleanDirectory(File directory) throws MojoExecutionException {
        try {
            FileUtils.deleteDirectory(directory);
            FileUtils.mkdir(directory.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to clean or create directory: " + directory, e);
        }
    }

}
