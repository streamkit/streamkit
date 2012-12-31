package org.streamkit.vod.album;

import java.lang.Exception;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mediacenter.testing.IntegrationTest;
import org.streamkit.vod.album.PostprocessingIntegrationTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 */
@Category(IntegrationTest.class)
public class AlbumJobIntegrationTest extends PostprocessingIntegrationTestBase
{
    @Test
    public void testAlbumQueries() throws Exception
    {
        final MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test description"));
        entity.addPart("title", new StringBody("test_video"));
        entity.addPart("album", new StringBody("test_album"));
        entity.addPart("mediaFile",
                new InputStreamBody(getClass().getResourceAsStream("/videos/video1.mp4"), "video1.mp4"));

        getRequestExecutor().execute(
                getRequestBuilder().buildPostRequest(demoChannelPath + "/vod/2012/9/1/test_video")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);

        Thread.sleep(6000);

        getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).assertContentContains("mix:shareable");

        // TODO: work in progress
        //assertAlbumQuery(demoChannelPath + "/albums/test_album", "");
    }

    private void assertAlbumQuery(String albumPath, String expectedResult) throws Exception
    {
        String requestString = albumPath + ".search.json";

        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(requestString)).getContent();
        assertEquals(jsonResponse, expectedResult);
    }


}
