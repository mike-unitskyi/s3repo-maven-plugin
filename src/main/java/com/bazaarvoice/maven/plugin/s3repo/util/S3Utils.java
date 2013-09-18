package com.bazaarvoice.maven.plugin.s3repo.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bazaarvoice.maven.plugin.s3repo.S3RepositoryPath;

import java.util.ArrayList;
import java.util.List;

public final class S3Utils {

    private S3Utils() {}

    public static String toRepoRelativePath(S3ObjectSummary summary, S3RepositoryPath s3RepositoryPath) {
        return bucketKeyToRepoRelativePath(s3RepositoryPath, summary.getKey());
    }

    private static String bucketKeyToRepoRelativePath(S3RepositoryPath s3RepositoryPath, String bucketKey) {
        return s3RepositoryPath.hasBucketRelativeFolder()
            ? bucketKey.replaceFirst("^\\Q" + s3RepositoryPath.getBucketRelativeFolder() + "/\\E", "")
            : bucketKey;
    }

    /** S3 may paginate object lists; this will walk through all pages and produce full result list. */
    public static List<S3ObjectSummary> listAllObjects(AmazonS3 s3Session, ListObjectsRequest request) {
        List<S3ObjectSummary> allResults = new ArrayList<S3ObjectSummary>();
        ObjectListing result = s3Session.listObjects(request);
        allResults.addAll(result.getObjectSummaries());
        while (result.isTruncated()) {
            result = s3Session.listNextBatchOfObjects(result);
            allResults.addAll(result.getObjectSummaries());
        }
        return allResults;
    }

}
