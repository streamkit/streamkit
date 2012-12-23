package org.streamkit.vod.album;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.sling.commons.testing.jcr.RepositoryTestBase;
import org.apache.sling.event.impl.DistributingEventHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mediacenter.resource.MediaCenterResourceType;
import org.streamkit.vod.AlbumService;

import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: ddascal
 * Date: 11/30/12
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(value = JUnit4.class)
public class AlbumServiceImplTest extends RepositoryTestBase
{
    private Node rootNode;
    private Node channelNode;

    private AlbumService albumService;


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

        albumService = new AlbumServiceImpl();
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

    @Test
    public void testNewVod_NewAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2011").addNode("12")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");

        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album");

        Node albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album/");
        assertNotNull(albumNode);
        assertEquals(albumNode.getProperty("sling:resourceType").getString(), MediaCenterResourceType.ALBUM);

        Node clonedVodNode = albumNode.getNode("2011/12/new_vod");
        assertNotNull(clonedVodNode);
        assertEquals(newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals(newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());
    }

    @Test
    public void testAddingSameVodTwice() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2011").addNode("10")
                .addNode("new_vod_twice", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod_twice");

        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album_twice");

        Node albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album_twice/");
        assertNotNull(albumNode);
        assertEquals(albumNode.getProperty("sling:resourceType").getString(), MediaCenterResourceType.ALBUM);

        Node clonedVodNode = albumNode.getNode("2011/10/new_vod_twice");
        assertNotNull(clonedVodNode);
        assertEquals(newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals(newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());

        albumService.addVideoToAlbum(newVod, "new_vod_album_twice");
        assertNotNull(albumNode);
        assertEquals(albumNode.getProperty("sling:resourceType").getString(), MediaCenterResourceType.ALBUM);
        clonedVodNode = albumNode.getNode("2011/10/new_vod_twice");
        assertNotNull(clonedVodNode);
        assertEquals(newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals(newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());
    }

    @Test
    public void testNewVod_ExistingAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2011").addNode("9")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");

        newVod.addMixin("mix:shareable");

        // first, make sure album exists
        Node albumNode = channelNode.addNode("album");
        albumNode.setProperty("sling:resourceType", MediaCenterResourceType.ALBUM);

        albumService.addVideoToAlbum(newVod, "new_vod_album");

        albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album/");
        assertNotNull(albumNode);
        Node clonedVodNode = albumNode.getNode("2011/9/new_vod");
        assertNotNull(clonedVodNode);
        assertEquals(newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals(newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());
    }

    @Test
    public void testRemoveVod_ExistingAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album");

        Node albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album/");

        albumService.removeVideoFromAlbum(newVod, albumNode.getPath());
    }

    @Test
    public void testRemoveVod_NonExistingAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.removeVideoFromAlbum(newVod, "non_existing_album");
    }

    @Test
    public void testRemoveWithWhiteList() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("10")
                .addNode("multi_album_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "multi_album_vod");
        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "album1");
        albumService.addVideoToAlbum(newVod, "album2");
        albumService.addVideoToAlbum(newVod, "album3");
        albumService.addVideoToAlbum(newVod, "album4");

        Node albumNode1 = rootNode.getNode("content/channel/demo/albums/album1/");
        Node albumNode2 = rootNode.getNode("content/channel/demo/albums/album2/");
        Node albumNode3 = rootNode.getNode("content/channel/demo/albums/album3/");
        Node albumNode4 = rootNode.getNode("content/channel/demo/albums/album4/");
        assertNotNull(albumNode1);
        assertNotNull(albumNode2);
        assertNotNull(albumNode3);
        assertNotNull(albumNode4);

        Node vodNode;
        vodNode = albumNode1.getNode("2012/10/multi_album_vod");
        assertNotNull(vodNode);
        vodNode = albumNode2.getNode("2012/10/multi_album_vod");
        assertNotNull(vodNode);
        vodNode = albumNode3.getNode("2012/10/multi_album_vod");
        assertNotNull(vodNode);
        vodNode = albumNode4.getNode("2012/10/multi_album_vod");
        assertNotNull(vodNode);

        albumService.removeVideoFromOtherAlbums(newVod, new ArrayList<String>(
                Arrays.asList("album2", "album3")));

        vodNode = albumNode2.getNode("2012/10/multi_album_vod");
        assertNotNull("album2 should contain the video", vodNode);
        vodNode = albumNode3.getNode("2012/10/multi_album_vod");
        assertNotNull("album3 should contain the video", vodNode);

        assertFalse("album1 should not have the video", albumNode1.hasNode("2012/10/multi_album_vod"));
        assertFalse("album4 should not have the video", albumNode4.hasNode("2012/10/multi_album_vod"));
    }

    @Test
    public void testRemoveVideoFromAllAlbums() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2009").addNode("10")
                .addNode("multi_album_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "multi_album_vod");
        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "album1");
        albumService.addVideoToAlbum(newVod, "album2");
        albumService.addVideoToAlbum(newVod, "album3");
        albumService.addVideoToAlbum(newVod, "album4");

        Node albumNode1 = rootNode.getNode("content/channel/demo/albums/album1/");
        Node albumNode2 = rootNode.getNode("content/channel/demo/albums/album2/");
        Node albumNode3 = rootNode.getNode("content/channel/demo/albums/album3/");
        Node albumNode4 = rootNode.getNode("content/channel/demo/albums/album4/");
        assertNotNull(albumNode1);
        assertNotNull(albumNode2);
        assertNotNull(albumNode3);
        assertNotNull(albumNode4);

        assertTrue("album1 should not have the video", albumNode1.hasNode("2009/10/multi_album_vod"));
        assertTrue("album2 should not have the video", albumNode4.hasNode("2009/10/multi_album_vod"));
        assertTrue("album3 should not have the video", albumNode4.hasNode("2009/10/multi_album_vod"));
        assertTrue("album4 should not have the video", albumNode4.hasNode("2009/10/multi_album_vod"));

        albumService.removeVideoFromOtherAlbums(newVod, new ArrayList<String>());
        assertFalse("album1 should not have the video", albumNode1.hasNode("2009/10/multi_album_vod"));
        assertFalse("album2 should not have the video", albumNode4.hasNode("2009/10/multi_album_vod"));
        assertFalse("album3 should not have the video", albumNode4.hasNode("2009/10/multi_album_vod"));
        assertFalse("album4 should not have the video", albumNode4.hasNode("2009/10/multi_album_vod"));
    }

    // negative tests
    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidResource() throws Exception
    {
        Node invalidChannelNode = rootNode.addNode("content").addNode("channel").addNode("invalid_channel");
        Node newVod = invalidChannelNode.addNode("vod").addNode("2012").addNode("1")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album");
    }

    @Test
    public void testRemoveVideoFromNullAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("5")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.removeVideoFromAlbum(newVod, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullResource() throws Exception
    {
        albumService.removeVideoFromAlbum(null, null);
    }


}
