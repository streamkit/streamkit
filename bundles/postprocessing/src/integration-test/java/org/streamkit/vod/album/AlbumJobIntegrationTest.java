package org.streamkit.vod.album;

import java.io.IOException;
import java.lang.Exception;
import java.lang.Thread;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mediacenter.testing.IntegrationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
@Category(IntegrationTest.class)
public class AlbumJobIntegrationTest extends PostprocessingIntegrationTestBase
{
    @Test
    public void testVideoIsAddedToAlbum() throws Exception
    {
        uploadNewVideo("first_video", "first_album");

        Thread.sleep(1000);

        getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/first_video.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).assertContentContains("mix:shareable");

        //test that video belongs to the album
        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/first_album.search.json")).getContent();
        assertTrue(jsonResponse.contains("\"name\":\"first_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"first_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/first_video\""));

        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + ".search.json")).getContent();
        assertTrue(jsonResponse.contains("\"name\":\"first_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"first_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/first_video\""));
    }

    @Test
    public void testVideoBelongingToMultipleAlbums() throws Exception
    {
        uploadNewVideo("video1_multi_album", "album1");
        uploadNewVideo("video2_multi_album", "album1,album2");
        uploadNewVideo("video3_multi_album", "album2");

        Thread.sleep(2000);

        // query album1
        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/album1.search.json")).getContent();
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video1_multi_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2_multi_album\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video3_multi_album\""));

        // query album2
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/album2.search.json")).getContent();
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video1_multi_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2_multi_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video3_multi_album\""));

        // query the whole channel
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + ".search.json")).getContent();
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video1_multi_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2_multi_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video3_multi_album\""));
    }

    @Test
    public void testAlbumSearch() throws Exception
    {
        uploadNewVideo("abc", "album1");
        uploadNewVideo("def", "album1,album2");
        uploadNewVideo("ghi", "album2");
        uploadNewVideo("jkl", "album2");

        Thread.sleep(10000);

        // query album1 for "abc" search term
        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/album1.search.json?q=abc")).getContent();
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/abc\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/def\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/ghi\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/jkl\""));

        //query album2 for "ghi"
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/album2.search.json?q=ghi")).getContent();
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/abc\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/def\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/ghi\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/jkl\""));

        //query album3 for "non-existing"
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/album2.search.json?q=non-existing")).getContent();
        assertEquals("[]", jsonResponse);
    }

    @Test
    public void testAddingVideosToExistingAlbum() throws Exception
    {
        uploadNewVideo("test_video", "test_album");

        Thread.sleep(1000);

        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/test_album.search.json")).getContent();
        assertTrue(jsonResponse.contains("\"name\":\"test_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"test_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/test_video\""));

        uploadNewVideo("video2", "");
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/test_album.search.json")).getContent();
        assertTrue(jsonResponse.contains("\"name\":\"test_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"test_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/test_video\""));
        // video2 should NOT be part of the response
        assertFalse(jsonResponse.contains("\"name\":\"video2\""));
        assertFalse(jsonResponse.contains("\"album\":\"\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2\""));

        uploadNewVideo("video3", "test_album");
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/test_album.search.json")).getContent();
        assertTrue(jsonResponse.contains("\"name\":\"test_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"test_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/test_video\""));
        //video3 should BE part of the response
        assertTrue(jsonResponse.contains("\"name\":\"video3\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video3\""));
        //video2 should NOT be part of the response
        assertFalse(jsonResponse.contains("\"name\":\"video2\""));
        assertFalse(jsonResponse.contains("\"album\":\"\""));
        assertFalse(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2\""));

        // test that video2 is part of demo channel content
        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + ".search.json")).getContent();
        assertTrue(jsonResponse.contains("\"name\":\"test_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"test_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/test_video\""));
        //video3 should BE part of the response
        assertTrue(jsonResponse.contains("\"name\":\"video3\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video3\""));
        //video2 should NOT be part of the response
        assertTrue(jsonResponse.contains("\"name\":\"video2\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2\""));

        // add video2 to test_album
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("album", new StringBody("test_album"));
        getRequestExecutor().execute(
                getRequestBuilder().buildPostRequest(demoChannelPath + "/vod/2012/9/1/video2")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(200);

        Thread.sleep(2000);

        jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/albums/test_album.search.json")).getContent();
        //test_video should BE part of the response
        assertTrue(jsonResponse.contains("\"name\":\"test_video\""));
        assertTrue(jsonResponse.contains("\"album\":\"test_album\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/test_video\""));
        //video3 should BE part of the response
        assertTrue(jsonResponse.contains("\"name\":\"video3\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video3\""));
        //video2 should BE part of the response NOW
        assertTrue(jsonResponse.contains("\"name\":\"video2\""));
        assertTrue(jsonResponse.contains("\"jcr:path\":\"/content/channel/demo/vod/2012/9/1/video2\""));
    }

    private void uploadNewVideo(String videoTitle, String videoAlbum) throws IOException
    {
        final MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test description"));
        entity.addPart("title", new StringBody(videoTitle));
        entity.addPart("album", new StringBody(videoAlbum));
        entity.addPart("mediaFile",
                new InputStreamBody(getClass().getResourceAsStream("/videos/video1.mp4"), "video1.mp4"));

        getRequestExecutor().execute(
                getRequestBuilder().buildPostRequest(demoChannelPath + "/vod/2012/9/1/" + videoTitle)
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);
    }


}
