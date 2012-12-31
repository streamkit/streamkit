package org.streamkit.vod;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.sling.commons.testing.jcr.RepositoryTestBase;
import org.apache.sling.event.impl.DistributingEventHandler;
import org.junit.After;
import org.junit.Before;
import org.mediacenter.resource.MediaCenterResourceType;

/**
 * Base class to be used when unit testing something that needs a JCR repository.
 * This class starts a new repository, adding a "demo" channel node
 */
public class MediaCenterRepositoryTestBase extends RepositoryTestBase
{
    protected Node rootNode;
    protected Node channelNode;

    @Before
    public void setupBefore() throws Exception
    {
        setUp();
        assertTrue(registerNodeType(getSession(),
                DistributingEventHandler.class.getResourceAsStream("/SLING-INF/nodetypes/resource.cnd")));

        rootNode = getTestRootNode();

        channelNode = rootNode.addNode("content");
        channelNode = channelNode.addNode("channel").addNode("demo");
        channelNode.setPrimaryType("nt:unstructured");
        channelNode.setProperty("sling:resourceType", MediaCenterResourceType.CHANNEL);
    }

    private boolean registerNodeType(Session session, InputStream resourceAsStream)
    {
        try
        {
            CndImporter.registerNodeTypes(new InputStreamReader(resourceAsStream, "UTF-8"), session);
            return true;
        }
        catch (Exception e)
        {
            // ignore
            return false;
        }
    }

    @After
    public void tearDownAfter() throws Exception
    {
        tearDown();
    }
}
