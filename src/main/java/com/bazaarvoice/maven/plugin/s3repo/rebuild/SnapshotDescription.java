package com.bazaarvoice.maven.plugin.s3repo.rebuild;

import com.bazaarvoice.maven.plugin.s3repo.S3RepositoryPath;

/** Basic snapshot description. */
final class SnapshotDescription {

    private final S3RepositoryPath s3RepositoryPath;
    private final String bucketName;
    /** Everything in the bucket key for the snapshot <em>before</em> the "SNAPSHOT" string. */
    private final String bucketKeyPrefix;
    /** The full bucket key for the SNAPSHOT. */
    private final String bucketKey;
    /** The number that follows the "SNAPSHOT" string in the file name. */
    private final int ordinal;

    public SnapshotDescription(S3RepositoryPath s3RepositoryPath, String bucketName, String bucketKeyPrefix, String bucketKey, int ordinal) {
        this.s3RepositoryPath = s3RepositoryPath;
        this.bucketName = bucketName;
        this.bucketKeyPrefix = bucketKeyPrefix;
        this.bucketKey = bucketKey;
        this.ordinal = ordinal;
    }

    public boolean existsInRepository(S3RepositoryPath repo) {
        return bucketName.equals(repo.getBucketName())
            && bucketKey.startsWith(repo.getBucketRelativeFolder());
    }

    public S3RepositoryPath getS3RepositoryPath() {
        return s3RepositoryPath;
    }

    public String getBucketName() {
        return bucketName;
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
