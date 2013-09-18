package com.bazaarvoice.maven.plugin.s3repo.list;

import com.amazonaws.services.s3.AmazonS3;
import com.bazaarvoice.maven.plugin.s3repo.S3RepositoryPath;
import com.bazaarvoice.maven.plugin.s3repo.support.LocalYumRepoFacade;

final class ListContext {

    private AmazonS3 s3Session;
    private S3RepositoryPath s3RepositoryPath;
    private LocalYumRepoFacade localYumRepo;

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

}
