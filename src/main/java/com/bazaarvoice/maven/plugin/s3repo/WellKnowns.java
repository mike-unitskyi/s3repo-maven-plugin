package com.bazaarvoice.maven.plugin.s3repo;

public final class WellKnowns {

    private WellKnowns() {}

    /** Well-known names. */
    public static final String YUM_REPODATA_FOLDERNAME = "repodata";
    public static final String YUM_REPOMETADATA_FILENAME = "repomd.xml";
    public static final String[] YUM_REPOMETADATA_FILE_TYPES = {"primary", "filelists", "other"};

    public static final int SOCKET_TIMEOUT = 30 * 1000;

}
