package com.bazaarvoice.maven.plugin.s3repo.support;

import com.bazaarvoice.maven.plugin.s3repo.WellKnowns;
import com.bazaarvoice.maven.plugin.s3repo.util.LogStreamConsumer;
import com.bazaarvoice.maven.plugin.s3repo.util.NullStreamConsumer;
import com.bazaarvoice.maven.plugin.s3repo.util.SimpleNamespaceResolver;
import com.bazaarvoice.maven.plugin.s3repo.util.XmlUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Encapsulates queries and operations over a local copy of a YUM repo. */
public final class LocalYumRepoFacade {

    private final File repositoryRoot;
    private final String createRepoCommand;
    private final Set<String> createRepoArgs;
    private final Log log;

    public LocalYumRepoFacade(File repositoryRoot, String createRepoCommand, String createRepoOpts, Log log) {
        this.log = log;
        this.repositoryRoot = repositoryRoot;
        this.createRepoCommand = createRepoCommand;

        ImmutableSet.Builder<String> opts = ImmutableSet.builder();
        if (StringUtils.isNotEmpty(createRepoOpts)) {
            for (String opt : createRepoOpts.split("\\s")) {
                if (StringUtils.isNotEmpty(opt)) {
                    Preconditions.checkArgument(opt.startsWith("-"), "Option \"" + opt + "\" invalid. You may only provide options, not arguments.");
                    opts.add(opt);
                }
            }
        }
        this.createRepoArgs = opts.build();
    }

    public boolean isRepoDataExists() {
        // as a heuristic, answer true if repo metadata file exists
        return determineRepoMetadataFile().isFile();
    }

    /**
     * Checks checksums of repo metadata files. Throws exception if files fail verification
     */
    public void verifyRepoDataFileChecksums() throws MojoExecutionException {
        File repoMetadataFile = determineRepoMetadataFile();
        if (!repoMetadataFile.isFile()) {
            throw new IllegalStateException("File didn't exist: " + repoMetadataFile.getPath());
        }
        Document repoMetadata = XmlUtils.parseXmlFile(repoMetadataFile);

        // check checksum of repo files
        for (String fileType : WellKnowns.YUM_REPOMETADATA_FILE_TYPES) {
            final File file = resolveMetadataFile(fileType, repoMetadata);
            try {
                final FileInputStream fileIn = new FileInputStream(file);
                try {
                    final Checksum checksum = resolveMetadataChecksum(fileType, repoMetadata);
                    String digest;
                    if ("sha".equals(checksum.checksumType) || "sha1".equals(checksum.checksumType)) {
                        digest = DigestUtils.shaHex(fileIn);
                    } else if ("sha256".equals(checksum.checksumType)) {
                        digest = DigestUtils.sha256Hex(fileIn);
                    } else if ("sha384".equals(checksum.checksumType)) {
                        digest = DigestUtils.sha384Hex(fileIn);
                    } else if ("sha512".equals(checksum.checksumType)) {
                        digest = DigestUtils.sha512Hex(fileIn);
                    } else if ("md5".equals(checksum.checksumType)) {
                        digest = DigestUtils.md5Hex(fileIn);
                    } else {
                        // default to sha256
                        digest = DigestUtils.sha256Hex(fileIn);
                    }
                    if (!checksum.checksumValue.equals(digest)) {
                        throw new MojoExecutionException("Checksum does not match for " + file.getPath() + ". Expected " + checksum.checksumValue + " but got " + digest);
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to calculate checksum for " + file.getPath(), e);
                } finally {
                    try { Closeables.close(fileIn, true);} catch (IOException e) {/*swallowed*/}
                }
            } catch (FileNotFoundException e) {
                throw new MojoExecutionException("Repo file " + file.getPath() + " not found");
            }
        }
    }

    public boolean hasFile(String repoRelativePath) {
        return new File(repositoryRoot, repoRelativePath).isFile();
    }

    /** Parse primary metadata file to get list of repo file paths (these paths will be *repo-relative*). */
    public List<String> parseFileListFromRepoMetadata() throws MojoExecutionException {
        File repoMetadataFile = determineRepoMetadataFile();
        if (!repoMetadataFile.isFile()) {
            throw new IllegalStateException("File didn't exist: " + repoMetadataFile.getPath());
        }
        return extractFileListFromPrimaryMetadataFile(
                XmlUtils.parseXmlFile(resolvePrimaryMetadataFile(XmlUtils.parseXmlFile(repoMetadataFile))));
    }

    /** Execute the createrepo command. */
    public void createRepo() throws MojoExecutionException {
        internalCreateRepo(false/*no update*/);
    }

    /** Execute the createrepo command in *update-only* mode. */
    public void updateRepo() throws MojoExecutionException {
        internalCreateRepo(true/*update*/);
    }

    public File repoDataDirectory() {
        return new File(repositoryRoot, WellKnowns.YUM_REPODATA_FOLDERNAME);
    }

    /** Execute the createrepo command. */
    private void internalCreateRepo(boolean updateOnly) throws MojoExecutionException {
        Commandline commandline = new Commandline();
        commandline.setExecutable(this.createRepoCommand);
        ImmutableSet.Builder<String> args = ImmutableSet.<String>builder().addAll(createRepoArgs);
        if (updateOnly) {
            //ensure that repo metadata is valid before updating
            log.info("Verifying repo metadata for update");
            verifyRepoDataFileChecksums();
            log.info("Successfully verified repo metadata for update");

            // if metadata already exists, we will execute "createrepo --update --skip-stat ."
            args.add("--update", "--skip-stat");
        }
        for (String arg : args.build()) {
            commandline.createArg().setValue(arg);
        }
        commandline.createArg().setValue(repositoryRoot.getPath());
        log.info("Executing \'" + commandline.toString() + "\'");
        try {
            int result = CommandLineUtils.executeCommandLine(commandline, NullStreamConsumer.theInstance, new LogStreamConsumer(log));
            if (result != 0) {
                throw new MojoExecutionException(createRepoCommand + " returned: \'" + result + "\' executing \'" + commandline + "\'");
            }
        } catch (CommandLineException e) {
            throw new MojoExecutionException("Unable to execute: " + commandline, e);
        }
        log.info("Successfully built repo using directory: " + repositoryRoot.getPath());
    }

    private List<String> extractFileListFromPrimaryMetadataFile(Document primaryMetadataFile) throws MojoExecutionException {
        // we will return a list of *repo-relative* file paths
        List<String> retval = new ArrayList<String>();
        String rootNamespaceUri = determineRootNamespaceUri(primaryMetadataFile);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(SimpleNamespaceResolver.forPrefixAndNamespace("common", rootNamespaceUri));
        NodeList repoRelativeFilePaths =
            (NodeList) evaluateXPathNodeSet(xpath, "//common:metadata/common:package/common:location/@href", primaryMetadataFile);
        for (int i = 0; i < repoRelativeFilePaths.getLength(); ++i) {
            retval.add(repoRelativeFilePaths.item(i).getNodeValue());
        }
        return retval;
    }

    /** Resolve repomd file (i.e., repodata/repomd.xml) file. */
    private File determineRepoMetadataFile() {
        // path to repomd.xml, e.g.
        return new File(repoDataDirectory(), WellKnowns.YUM_REPOMETADATA_FILENAME);
    }

    private File resolvePrimaryMetadataFile(Document metadata) throws MojoExecutionException {
        return resolveMetadataFile("primary", metadata);
    }

    private File resolveMetadataFile(String type, Document metadata) throws MojoExecutionException {
        // determine root namespace for use in xpath queries
        String rootNamespaceUri = determineRootNamespaceUri(metadata);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(SimpleNamespaceResolver.forPrefixAndNamespace("repo", rootNamespaceUri));
        // metadata file, relative to *repository* root
        String repoRelativeMetadataFilePath =
                evaluateXPathString(xpath, "//repo:repomd/repo:data[@type='" + type + "']/repo:location/@href", metadata);
        // determine metadata file (e.g., "repodata/primary.xml.gz")
        File metadataFile = new File(repositoryRoot, repoRelativeMetadataFilePath);
        if (!metadataFile.isFile() || !metadataFile.getName().endsWith(".gz")) {
            throw new MojoExecutionException(type + " metadata file, '" + metadataFile.getPath() +
                    "', does not exist or does not have .gz extension");
        }
        return metadataFile;
    }

    private Checksum resolveMetadataChecksum(String type, Document metadata) throws MojoExecutionException {
        // determine root namespace for use in xpath queries
        String rootNamespaceUri = determineRootNamespaceUri(metadata);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(SimpleNamespaceResolver.forPrefixAndNamespace("repo", rootNamespaceUri));
        return new Checksum(
                evaluateXPathString(xpath, "//repo:repomd/repo:data[@type='" + type + "']/repo:checksum/@type", metadata),
                evaluateXPathString(xpath, "//repo:repomd/repo:data[@type='" + type + "']/repo:checksum", metadata)
        );
    }

    private static Object evaluateXPathNodeSet(XPath xpath, String expression, Document document) throws MojoExecutionException {
        try {
            return xpath.evaluate(expression, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException(expression, e);
        }
    }

    private static String evaluateXPathString(XPath xpath, String expression, Document document) throws MojoExecutionException {
        try {
            return xpath.evaluate(expression, document);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException(expression, e);
        }
    }

    private static String determineRootNamespaceUri(Document metadata) {
        return metadata.getChildNodes().item(0).getNamespaceURI();
    }

    private static class Checksum {
        private final String checksumType;
        private final String checksumValue;

        private Checksum(final String checksumType, final String checksumValue) {
            this.checksumType = checksumType;
            this.checksumValue = checksumValue;
        }
    }
}
