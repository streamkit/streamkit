package org.apache.sling.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.fs.local.FileUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.jcr.*;
import java.io.File;

/**
 * @author Cosmin Stanciu
 */

public abstract class AbstractJackrabbitTestCase {

    private static Repository repository;
    public Session session;

    private final static String REPOSITORY_CONFIG_PATH = "src/test/resources/repository.xml";
    private final static String REPOSITORY_DIRECTORY_PATH = "target/repository";
//    private final static String REPOSITORY_CONFIG_PATH = "services/trunk/bundles/postprocessing/src/test/resources/repository.xml";
//    private final static String REPOSITORY_DIRECTORY_PATH = "services/trunk/bundles/postprocessing/target/repository";
    private final static File JACKRABBIT_DIRECTORY_PATH = new File(REPOSITORY_DIRECTORY_PATH);


    @BeforeClass
    public static void beforeAll() throws Exception {
        // Clean up the test data ...
        // FileUtil.delete(JACKRABBIT_DIRECTORY_PATH);

        repository = new TransientRepository(REPOSITORY_CONFIG_PATH, REPOSITORY_DIRECTORY_PATH);
    }

    @AfterClass
    public static void afterAll() throws Exception {
        try {
            JackrabbitRepository jackrabbit = (JackrabbitRepository) repository;
            jackrabbit.shutdown();
        } finally {
            // Clean up the test data ...
            if (JACKRABBIT_DIRECTORY_PATH.exists())
                FileUtil.delete(JACKRABBIT_DIRECTORY_PATH);
        }
    }

    public void startRepository() throws Exception {
        if (session == null) {
            session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        }
    }

    @After
    public void shutdownRepository() throws Exception {
        if (session != null) {
            try {
                session.logout();
            } finally {
                session = null;
            }
        }
    }

}
