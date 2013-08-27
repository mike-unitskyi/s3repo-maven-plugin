package com.bazaarvoice.maven.plugin.s3repo.rebuild;

import com.amazonaws.services.s3.AmazonS3;
import com.bazaarvoice.maven.plugin.s3repo.S3RepositoryPath;
import com.bazaarvoice.maven.plugin.s3repo.support.LocalYumRepoFacade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RebuildContext {

    private AmazonS3 s3Session;
    private S3RepositoryPath s3RepositoryPath;
    private LocalYumRepoFacade localYumRepo;
    /**
     * Here we keep track of a Map of bucket key *prefixes* to full bucket keys that represent SNAPSHOTS of
     * the same artifact.  For example, we may discover these files in the repository:
     *
     *      - path/to/awesome-artifact-1.4-SNAPSHOT1.noarch.rpm
     *      - path/to/awesome-artifact-1.4-SNAPSHOT-2.noarch.rpm
     *      - path/to/awesome-artifact-1.4-SNAPSHOT-blah.noarch.rpm
     *
     * This will result in the following map entry:
     *      path/to/awesome-artifact-1.4 => [ path/to/awesome-artifact-1.4-SNAPSHOT1.noarch.rpm, ...]
     *
     * The heuristic used here looks for any artifacts that contain the word "SNAPSHOT" and uses all characters
     * before this string as the key.
     */
    private final Map<String, List<SnapshotDescription>> bucketKeyPrefixToSnapshots = new HashMap<String, List<SnapshotDescription>>();
    private final List<SnapshotDescription> snapshotsToDeleteRemotely = new ArrayList<SnapshotDescription>();
    private final List<RemoteSnapshotRename> snapshotsToRenameRemotely = new ArrayList<RemoteSnapshotRename>();
    /** Repo-relative file paths that we will delete remotely. */
    private final Set<String> excludedFiles = new HashSet<String>();

    public AmazonS3 getS3Session() {
        return s3Session;
    }

    public void setS3Session(AmazonS3 s3Session) {
        this.s3Session = s3Session;
    }

    public void setS3RepositoryPath(S3RepositoryPath s3RepositoryPath) {
        this.s3RepositoryPath = s3RepositoryPath;
    }

    public S3RepositoryPath getS3RepositoryPath() {
        return s3RepositoryPath;
    }

    public LocalYumRepoFacade getLocalYumRepo() {
        return localYumRepo;
    }

    public void setLocalYumRepo(LocalYumRepoFacade localYumRepo) {
        this.localYumRepo = localYumRepo;
    }

    public void addSnapshotDescription(SnapshotDescription snapshotDescription) {
        List<SnapshotDescription> existing = bucketKeyPrefixToSnapshots.get(snapshotDescription.getBucketKeyPrefix());
        if (existing == null) {
            existing = new ArrayList<SnapshotDescription>();
            bucketKeyPrefixToSnapshots.put(snapshotDescription.getBucketKeyPrefix(), existing);
        }
        existing.add(snapshotDescription);
    }

    public Map<String, List<SnapshotDescription>> getBucketKeyPrefixToSnapshots() {
        return bucketKeyPrefixToSnapshots;
    }

    public void addSnapshotToDelete(SnapshotDescription toDelete) {
        snapshotsToDeleteRemotely.add(toDelete);
    }

    public List<SnapshotDescription> getSnapshotsToDeleteRemotely() {
        return snapshotsToDeleteRemotely;
    }

    public void addSnapshotToRename(RemoteSnapshotRename toRename) {
        snapshotsToRenameRemotely.add(toRename);
    }

    public List<RemoteSnapshotRename> getSnapshotsToRenameRemotely() {
        return snapshotsToRenameRemotely;
    }

    public void setExcludedFiles(List<String> repoRelativePaths) {
        excludedFiles.clear();
        excludedFiles.addAll(repoRelativePaths);
    }

    /** Repo-relative paths. */
    public Set<String> getExcludedFiles() {
        return excludedFiles;
    }

}
