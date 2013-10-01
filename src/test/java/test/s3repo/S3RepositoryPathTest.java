package test.s3repo;

import com.bazaarvoice.maven.plugin.s3repo.S3RepositoryPath;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test
public class S3RepositoryPathTest {

    public void testBucketOnly() {
        final S3RepositoryPath path = S3RepositoryPath.parse("/Bucket");
        assertEquals(path.getBucketName(), "Bucket");
        assertEquals(path.getBucketRelativeFolder(), "");
        assertFalse(path.hasBucketRelativeFolder());
    }

    public void testSubfolder() {
        final S3RepositoryPath path = S3RepositoryPath.parse("/Bucket/subfolder");
        assertEquals(path.getBucketName(), "Bucket");
        assertEquals(path.getBucketRelativeFolder(), "subfolder");
        assertTrue(path.hasBucketRelativeFolder());
    }

    public void testWithProtocol() {
        S3RepositoryPath path; // reusable

        path = S3RepositoryPath.parse("s3://Bucket");
        assertEquals(path.getBucketName(), "Bucket");
        assertEquals(path.getBucketRelativeFolder(), "");
        assertFalse(path.hasBucketRelativeFolder());

        path = S3RepositoryPath.parse("s3://Bucket/subfolder");
        assertEquals(path.getBucketName(), "Bucket");
        assertEquals(path.getBucketRelativeFolder(), "subfolder");
        assertTrue(path.hasBucketRelativeFolder());

        path = S3RepositoryPath.parse("s3://Bucket/sub/folder");
        assertEquals(path.getBucketName(), "Bucket");
        assertEquals(path.getBucketRelativeFolder(), "sub/folder");
        assertTrue(path.hasBucketRelativeFolder());

        path = S3RepositoryPath.parse("s3://Bucket/sub/folder/");
        assertEquals(path.getBucketName(), "Bucket");
        assertEquals(path.getBucketRelativeFolder(), "sub/folder");
        assertTrue(path.hasBucketRelativeFolder());
    }

    public void testFailures() {
        assertFailsToParse("s3:/Bucket");
        assertFailsToParse("subfolder");
        assertFailsToParse("Bucket/subfolder");
        assertFailsToParse("/");
        assertFailsToParse("");
    }

    private void assertFailsToParse(String value) {
        try {
            S3RepositoryPath.parse(value);
            fail("expected failure: " + value);
        } catch (Exception e) {
            // expected
        }
    }

}
