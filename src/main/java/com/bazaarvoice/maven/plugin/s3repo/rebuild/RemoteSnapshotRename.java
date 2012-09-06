package com.bazaarvoice.maven.plugin.s3repo.rebuild;

final class RemoteSnapshotRename {

    static RemoteSnapshotRename withNewBucketKey(SnapshotDescription source, String newBucketKey) {
        return new RemoteSnapshotRename(source, newBucketKey);
    }

    private final SnapshotDescription source;
    private final String newBucketKey;

    private RemoteSnapshotRename(SnapshotDescription source, String newBucketKey) {
        this.source = source;
        this.newBucketKey = newBucketKey;
    }

    public SnapshotDescription getSource() {
        return source;
    }

    public String getNewBucketKey() {
        return newBucketKey;
    }

}
