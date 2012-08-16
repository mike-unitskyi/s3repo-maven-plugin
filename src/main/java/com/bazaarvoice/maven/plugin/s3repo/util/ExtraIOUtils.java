package com.bazaarvoice.maven.plugin.s3repo.util;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public final class ExtraIOUtils {

    private ExtraIOUtils() {}

    public static String relativize(File directory, File file) throws MojoExecutionException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file);
        }
        try {
            String canonical = file.getCanonicalPath();
            String canonicalPrefix = directory.getCanonicalPath();
            if (!canonical.startsWith(canonicalPrefix)) {
                throw new MojoExecutionException("Couldn't relativize file path: " + canonicalPrefix + ", " + canonical);
            }
            // assert: canonicalPrefix is a prefix to canonical
            String relativized = canonical.substring(canonicalPrefix.length());
            if (relativized.endsWith(File.separator)) {
                // strip ending file separator if present
                relativized = relativized.substring(0, relativized.length() - 1);
            }
            return relativized;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to relativize file: " + directory + "," + file, e);
        }
    }

    public static Collection<File> listAllFiles(File directory) {
        return FileUtils.listFiles(directory, null/*list all files*/, true/*recurse into subdirs*/);
    }

    public static void touch(File file) throws MojoExecutionException {
        try {
            FileUtils.touch(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't touch file: " + file, e);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
