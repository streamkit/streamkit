package org.mediacenter.resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.sling.commons.testing.jcr.MockNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(value = JUnit4.class)
public class ChannelNodeLookupTest
{

    @Test
    public void testWithSimpleChannelPath()
            throws ValueFormatException, LockException, VersionException, ConstraintViolationException, RepositoryException
    {
        Node channelNode = new HierarchicalMockNode("/content/channel/demo");
        channelNode.setProperty("sling:resourceType", MediaCenterResourceType.CHANNEL);
        channelNode.setProperty("title", "demo");

        Node videoNode = channelNode.addNode("2012/11/28/simple_vod");
        videoNode.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        videoNode.setProperty("title", "simple_vod");

        Node result = ChannelNodeLookup.getClosestChannelInNode(videoNode);

        assertNotNull("result shouldn't be null", result );
        assertEquals( result.getProperty("title").getString(), "demo");
    }

    @Test
    public void testWithNullNode() throws Exception
    {
        Node result = ChannelNodeLookup.getClosestChannelInNode(null);
        assertNull("result should be null", result);

    }

    @Test
    public void testWithNestedChannels() throws Exception
    {
        Node channelNode = new HierarchicalMockNode("/content/channel/demo");
        channelNode.setProperty("sling:resourceType", MediaCenterResourceType.CHANNEL);
        channelNode.setProperty("title", "demo");

         Node videoNode = channelNode.addNode("2012/11/28/simple_vod");
        videoNode.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        videoNode.setProperty("title", "simple_vod");

        Node subchannelNode = channelNode.addNode("subchannel/demo_subchannel");
        subchannelNode.setProperty("sling:resourceType", MediaCenterResourceType.CHANNEL);
        subchannelNode.setProperty("title","demo_subchannel");

        videoNode = subchannelNode.addNode("2012/11/28/simple_vod_in_subchannel");
        videoNode.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        videoNode.setProperty("title", "simple_vod_in_subchannel");

        Node result = ChannelNodeLookup.getClosestChannelInNode(videoNode);

        assertNotNull("result shouldn't be null", result );
        assertEquals( result.getProperty("title").getString(), "demo_subchannel");
    }

    /*@Test(expected = IllegalArgumentException.class)
    public void testWithError()
    {

    }*/
}
