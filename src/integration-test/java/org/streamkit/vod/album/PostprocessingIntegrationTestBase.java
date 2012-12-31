package org.streamkit.vod.album;

import org.apache.sling.testing.tools.sling.SlingClient;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.fail;

public class PostprocessingIntegrationTestBase extends SlingTestBase
{
    final protected String demoChannelPath = "/content/channel/demo";

    @Before
    public void setupDemoChannel() throws Exception {
        final SlingClient slingClient = new SlingClient(getServerBaseUrl(), ADMIN, ADMIN);
        try {
            slingClient.createNode(demoChannelPath, "sling:resourceType", "mediacenter:channel");
        }
        catch (Exception e) {
            fail("Exception while setting up DEMO channel: " + e);
        }
    }

    @After
    public void deleteDemoChannel() {
        final SlingClient slingClient = new SlingClient(getServerBaseUrl(), ADMIN, ADMIN);
        try {
            slingClient.delete(demoChannelPath);
        }
        catch (Exception e) {
            fail("Exception while cleaning up DEMO channel: " + e);
        }
    }
}