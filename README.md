s3repo-maven-plugin
===================

Plugin allowing you to add arbitrary dependencies to an S3 Yum Repository.  Also has a goal to support rebuilding S3 YUM
repositories.

Note that using S3 as a YUM repository is possible via the YUM plugin yum-s3-plugin
(https://github.com/jbraeuer/yum-s3-plugin).

Goals
=====

* __create-update__ - Creates or updates an S3 YUM repository.
* __rebuild-repo__ - Rebuilds an existing S3 YUM repository, optionally eliminating old SNAPSHOT artifacts.

create-update: Usage Example
============================

Here is a common configuration:

    <plugin>
        <groupId>com.bazaarvoice.maven.plugins</groupId>
        <artifactId>s3repo-maven-plugin</artifactId>
        <version>1.1</version> <!-- use latest version instead -->
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>create-update</goal>
                </goals>
                <configuration>
                    <s3RepositoryPath>/MyBucket/myRepository</s3RepositoryPath>
                    <artifactItems>
                        <artifactItem>
                            <groupId>${yourGroupdId}</groupId>
                            <artifactId>${yourArtifactId}</artifactId>
                            <version>${yourVersion}</version>
                            <type>rpm</type>
                            <classifier>rpm</classifier>
                            <targetExtension>noarch.rpm</targetExtension>
                        </artifactItem>
                    </artifactItems>
                </configuration>
            </execution>
        </executions>
    </plugin>

create-update: Special Notes
============================

* If one of your declared artifact items *already* exists in your S3 YUM repository, the goal will fail unless the declared
  artifact item is a *SNAPSHOT* dependency and the "autoIncrementSnapshotArtifacts" configuration property is true (this
is the default value/behavior).
* You can use "-Ds3repo.allowCreateRepository=true" the first time you run the plugin to initialize a new repository; subsequent
  runs for a project can leave this value at its default (false) for extra safety.

create-update: Full Usage Example
=================================

Here is a full configuration demonstrating all possible configuration options. See comments for further explanation:

    <plugin>
        <groupId>com.bazaarvoice.maven.plugins</groupId>
        <artifactId>s3repo-maven-plugin</artifactId>
        <version>1.1</version> <!-- use latest version instead -->
        <executions>
            <execution>
                <phase>deploy</phase> <!-- phase is optional; deploy is the default -->
                <goals>
                    <goal>create-update</goal>
                </goals>
                <configuration>
                    <!--
                        Optional. Allow this plugin to create the repository directory in S3 if it doesn't exist.
                        The default is false. Note that the target *bucket* must always exist; this plugin will
                        *never* create a bucket on your behalf.
                    -->
                    <allowCreateRepository>true</allowCreateRepository>
                    <!--
                        Optional. Whether or not to auto-increment snaphsot artifacts. For example, if your repository
                        contains foo-1.0-SNAPSHOT.noarch.rpm and you are attempting to deploy this artifact again, a numeric
                        index will be added to your snapshot to avoid name collision (e.g., foo-1.0-SNAPSHOT1.noarch.rpm).
                        The default value is true.
                    -->
                    <autoIncrementSnapshotArtifacts>true</autoIncrementSnapshotArtifacts>
                    <!--
                        Optional. You may wish to perform a "dry run" execution without uploading any files to your repository.
                        You will likely do this from the command-line using "-Ds3repo.doNotUpload=true".
                    -->
                    <doNotUpload>false</doNotUpload>
                    <!--
                        Optional. You may need to specify an alternate path for the "createrepo" command.
                    -->
                    <createrepo>/usr/bin/createrepo</createrepo>
                    <!--
                        The S3 path to your repository. The first path entry is the *bucket*; optional
                        subpaths may indicate a repository that is not at the root/bucket level.
                        Examples; any of these are valid:
                            /MyBucket/myRepository
                            /MyBucket
                            s3://MyBucket/myRepository
                    -->
                    <s3RepositoryPath>/MyBucket/myRepository</s3RepositoryPath>
                    <!--
                        You can specify your access and secret keys in the POM but this is unadvised.
                        Use "-Ds3repo.accessKey=YOURKEY -Ds3repo.secretKey=YOURKEY" on the command-line instead.
                    -->
                    <s3AccessKey>${yourS3AccessKey}</s3AccessKey>
                    <s3SecretKey>${yourS3SecretKey}</s3SecretKey>
                    <!--
                        You can specify multiple artifact items to copy to the repo.
                    -->
                    <artifactItems>
                        <artifactItem>
                            <groupId>${yourGroupId}</groupId>
                            <artifactId>${yourArtifactId}</artifactId>
                            <version>${yourVersion}</version>
                            <type>rpm</type>
                            <classifier>rpm</classifier>
                            <!--
                                Optional target extension to use for your artifact; it defaults to "noarch.rpm".
                                The artifact name that is added to the repository will be <artifactId>-<version>-<targetExtension>.
                            -->
                            <targetExtension>noarch.rpm</targetExtension>
                            <!--
                                You may optionally specify a target folder for each artifactItem.
                                This is useful for organizing separate projects' packages/rpms into separate folders in
                                a single repository.
                             -->
                            <targetSubfolder>myProject</targetSubfolder>
                        </artifactItem>
                    </artifactItems>
                </configuration>
            </execution>
        </executions>
    </plugin>

rebuild-repo: Usage Example
===========================

A simple example:

    $ mvn com.bazaarvoice.maven.plugins:s3repo-maven-plugin:1.1:rebuild-repo \
        -Ds3repo.repositoryPath=s3://BucketName/yum-repo \
        -Ds3repo.accessKey=ABC \
        -Ds3repo.secretKey=DEF

If you want to clean up old snapshots, use:

    $ mvn com.bazaarvoice.maven.plugins:s3repo-maven-plugin:1.1:rebuild-repo \
        -Ds3repo.repositoryPath=s3://BucketName/yum-repo \
        -Ds3repo.accessKey=ABC \
        -Ds3repo.secretKey=DEF \
        -Ds3repo.removeOldSnapshots=true

A verbose example:

    $ mvn com.bazaarvoice.maven.plugins:s3repo-maven-plugin:1.1:rebuild-repo \
        -Ds3repo.repositoryPath=s3://BucketName/yum-repo \
        -Ds3repo.accessKey=ABC \
        -Ds3repo.secretKey=DEF \
        -Ds3repo.removeOldSnapshots=true \
        -Ds3repo.doNotValidate=true \
        -Ds3repo.doNotUpload=true \
        -Ds3repo.createrepo=/usr/bin/createrepo

Use "s3repo.doNotUpload" to rebuild the repository locally but not upload it. Use "s3repo.doNotValidate"
to rebuild the repository but not fail if existing repo metadata is missing or corrupt.
