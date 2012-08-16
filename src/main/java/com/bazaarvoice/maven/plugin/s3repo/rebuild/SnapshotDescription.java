package com.bazaarvoice.maven.plugin.s3repo.rebuild;

import java.util.Date;

/** Basic snapshot description. */
final class SnapshotDescription {

    private final String bucketKeyPrefix;
    private final String bucketKey;
    private final Date lastModified;

    public SnapshotDescription(String bucketKeyPrefix, String bucketKey, Date lastModified) {
        this.bucketKeyPrefix = bucketKeyPrefix;
        this.bucketKey = bucketKey;
        this.lastModified = lastModified;
    }

    public String getBucketKeyPrefix() {
        return bucketKeyPrefix;
    }

    public String getBucketKey() {
        return bucketKey;
    }

    public Date getLastModified() {
        return lastModified;
    }

}
