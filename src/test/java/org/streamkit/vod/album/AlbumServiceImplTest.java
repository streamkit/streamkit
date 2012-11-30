package org.streamkit.vod.album;

import java.io.InputStream;
import java.io.InputStreamReader;
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
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");

        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album");

        Calendar now = Calendar.getInstance();
        String year = String.valueOf(now.get(Calendar.YEAR));
        String month = String.valueOf(now.get(Calendar.MONTH) + 1);

        Node albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album/");
        assertNotNull(albumNode);
        assertEquals( albumNode.getProperty("sling:resourceType").getString(), MediaCenterResourceType.ALBUM);

        Node clonedVodNode = albumNode.getNode( year + "/" + month + "/new_vod");
        assertNotNull( clonedVodNode );
        assertEquals(newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals( newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());
    }

    @Test
    public void testNewVod_ExistingAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");

        newVod.addMixin("mix:shareable");

        // first, make sure album exists
        Calendar now = Calendar.getInstance();
        String year = String.valueOf(now.get(Calendar.YEAR));
        String month = String.valueOf(now.get(Calendar.MONTH) + 1);
        Node albumNode = channelNode.addNode("album");
        albumNode.setProperty("sling:resourceType", MediaCenterResourceType.ALBUM);

        albumService.addVideoToAlbum(newVod, "new_vod_album");

        albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album/");
        assertNotNull(albumNode);
        Node clonedVodNode = albumNode.getNode( year + "/" + month + "/new_vod");
        assertNotNull( clonedVodNode );
        assertEquals( newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals( newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());
    }

    @Test
    public void testRemoveVod_ExistingAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album");


        Calendar now = Calendar.getInstance();
        String year = String.valueOf(now.get(Calendar.YEAR));
        String month = String.valueOf(now.get(Calendar.MONTH) + 1);

        Node albumNode = rootNode.getNode("content/channel/demo/albums/new_vod_album/");

        albumService.removeVideoFromAlbum(newVod, albumNode.getPath() );
    }
    @Test
    public void testRemoveVod_NonExistingAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.removeVideoFromAlbum( newVod, "non_existing_album");
    }

    // negative tests
    @Test(expected = IllegalArgumentException.class)
    public void testWithInvalidResource() throws Exception
    {
        Node invalidChannelNode = rootNode.addNode("content").addNode("channel").addNode("invalid_channel");
        Node newVod = invalidChannelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.addVideoToAlbum(newVod, "new_vod_album");
    }

    @Test
    public void testRemoveVideoFromNullAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2012").addNode("12").addNode("01")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.addMixin("mix:shareable");

        albumService.removeVideoFromAlbum( newVod, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullResource() throws Exception
    {
        albumService.removeVideoFromAlbum(null, null);
    }


}
