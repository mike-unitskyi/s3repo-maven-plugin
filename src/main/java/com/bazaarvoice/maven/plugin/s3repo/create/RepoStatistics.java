package com.bazaarvoice.maven.plugin.s3repo.create;

import org.apache.maven.plugin.MojoExecutionException;


public class RepoStatistics {
    static RepoStatistics createRepoStatisticsFromCreateOrUpdateContext(CreateOrUpdateContext context) throws MojoExecutionException {
        if (context.getLocalYumRepo().isRepoDataExists()) {
            return new RepoStatistics(context.getLocalYumRepo().parseFileListFromRepoMetadata().size());
        } else {
            return new RepoStatistics(0);
        }
    }

    private final int numPackages;

    private RepoStatistics(final int numPackages) {
        this.numPackages = numPackages;
    }

    public int getNumPackages() {
        return numPackages;
    }
}

