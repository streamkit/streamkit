package org.streamkit.vod.jobs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.naming.NamingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mediacenter.resource.MediaCenterResourceType;
import org.osgi.service.component.ComponentContext;
import org.streamkit.vod.MediaCenterRepositoryTestBase;
import org.streamkit.vod.album.AlbumServiceImpl;


@RunWith(value = JUnit4.class)
public class AlbumJobTest extends MediaCenterRepositoryTestBase
{
    private AlbumJob albumJob;

    Method updateAlbumForVideoOP;

    @Override
    public void setupBefore() throws Exception
    {
        super.setupBefore();

        albumJob = new AlbumJob();

        Field albumServiceField = getField("albumService");
        albumServiceField.set(albumJob, new AlbumServiceImpl());

        updateAlbumForVideoOP = getMethod("updateAlbumForVideo", String.class, Session.class);
    }

    private Method getMethod(String name, Class... parameterTypes)
    {
        try
        {
            Method m = AlbumJob.class.getDeclaredMethod(name,
                    parameterTypes);
            m.setAccessible(true);
            return m;
        }
        catch (Throwable t)
        {
            fail(t.toString());
            return null; // compiler wants this
        }
    }

    private Field getField(String name)
    {
        try
        {
            Field f = AlbumJob.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }
        catch (NoSuchFieldException e)
        {
            fail(e.getMessage());
            return null;
        }
    }

    @Test
    public void testNewVod_newAlbum() throws Exception
    {
        Node newVod = channelNode.addNode("vod").addNode("2011").addNode("12")
                .addNode("new_vod", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "new_vod");
        newVod.setProperty("album", "new_album_new_vod");

        assertAlbumContentCount(channelNode.getPath() + "/albums/new_album_new_vod/", 0);
        updateAlbumForVideoOP.invoke(albumJob, newVod.getPath(), getSession());

        Node albumNode = rootNode.getNode("content/channel/demo/albums/new_album_new_vod/");
        assertNotNull(albumNode);
        assertEquals(albumNode.getProperty("sling:resourceType").getString(), MediaCenterResourceType.ALBUM);

        Node clonedVodNode = albumNode.getNode("2011/12/new_vod");
        assertNotNull(clonedVodNode);
        assertEquals(newVod.getProperty("jcr:uuid").getString(), clonedVodNode.getProperty("jcr:uuid").getString());
        assertEquals(newVod.getProperty("title").getString(), clonedVodNode.getProperty("title").getString());

        assertAlbumContentCount(channelNode.getPath() + "/albums/new_album_new_vod/", 1);
    }

    @Test
    public void testAddingMultipleVods() throws Exception
    {
        // add vod_1 to album_1
        Node newVod = channelNode.addNode("vod").addNode("2011").addNode("11")
                .addNode("vod_1", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "vod_1");
        newVod.setProperty("album", "album_1");

        assertAlbumContentCount(channelNode.getPath() + "/albums/album_1/", 0);
        updateAlbumForVideoOP.invoke(albumJob, newVod.getPath(), getSession());
        // in Sling, after adding a new content to an album, a new Event gets fired for the added content
        updateAlbumForVideoOP.invoke(albumJob, channelNode.getPath() + "/albums/album_1/2011/11/vod_1", getSession());

        assertAlbumContentCount(channelNode.getPath() + "/albums/album_1", 1);

        // add vod_2 without attaching it to any album
        newVod = channelNode.getNode("vod/2011/11").addNode("vod_2", "nt:unstructured");
        newVod.setProperty("sling:resourceType", MediaCenterResourceType.VOD);
        newVod.setProperty("title", "vod_2");

        updateAlbumForVideoOP.invoke(albumJob, newVod.getPath(), getSession());
        assertAlbumContentCount(channelNode.getPath() + "/albums/album_1", 1);

        //remove vod_1 from any albums
        newVod = channelNode.getNode("vod/2011/11/vod_1");
        newVod.setProperty("album", "");
        updateAlbumForVideoOP.invoke(albumJob, newVod.getPath(), getSession());
        assertAlbumContentCount(channelNode.getPath() + "/albums/album_1/", 0);

        // add vod_2 to album_1
        newVod = channelNode.getNode("vod/2011/11/vod_2");
        newVod.setProperty("album", "album_1");
        updateAlbumForVideoOP.invoke(albumJob, newVod.getPath(), getSession());
        assertAlbumContentCount(channelNode.getPath() + "/albums/album_1/", 1);
    }

    private void assertAlbumContentCount(String albumPath, int expectedVods) throws Exception
    {
        QueryManager queryManager = getSession().getWorkspace().getQueryManager();
        String expression = "SELECT * from [nt:unstructured] as x " +
                "where ISDESCENDANTNODE(x,[" + albumPath + "]) " +
                "and x.[sling:resourceType]='mediacenter:vod'"; // order by x.'jcr:created' DESC";

        Query query = queryManager.createQuery(expression, Query.JCR_SQL2);
        QueryResult result = query.execute();
        Iterator nodesIterator = result.getRows();
        int i = 0;
        while (nodesIterator.hasNext())
        {
            i++;
            nodesIterator.next();
        }

        assertEquals("Album query returned wrong number of vods", expectedVods, i);
    }


}
