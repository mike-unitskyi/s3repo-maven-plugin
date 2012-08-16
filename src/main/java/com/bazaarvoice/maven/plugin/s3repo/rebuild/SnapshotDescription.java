package com.bazaarvoice.maven.plugin.s3repo.rebuild;

/** Basic snapshot description. */
final class SnapshotDescription {

    private final String bucketKeyPrefix;
    private final String bucketKey;
    private final int ordinal;

    public SnapshotDescription(String bucketKeyPrefix, String bucketKey, int ordinal) {
        this.bucketKeyPrefix = bucketKeyPrefix;
        this.bucketKey = bucketKey;
        this.ordinal = ordinal;
    }

    public String getBucketKeyPrefix() {
        return bucketKeyPrefix;
    }

    public String getBucketKey() {
        return bucketKey;
    }

    public int getOrdinal() {
        return ordinal;
    }

}
