package org.streamkit.vod.jobs;

import java.io.IOException;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mediacenter.testing.IntegrationTest;
import org.streamkit.vod.album.PostprocessingIntegrationTestBase;

import static org.junit.Assert.assertTrue;


@Category(IntegrationTest.class)
public class VodEntityJobIntegrationTest extends PostprocessingIntegrationTestBase
{
    @Test
    public void testVideoHasCreatedField() throws Exception
    {
        uploadNewVideo("first_video_created", "first_album", true);

        Thread.sleep(1000);

        getRequestExecutor().execute(
                        getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/first_video_created.json")
                ).assertContentContains("\"created\":");

        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/first_video_created.json")
        ).getContent();

        assertTrue(jsonResponse.contains("\"title\":\"first_video_created\""));
        assertTrue(jsonResponse.contains("\"album\":\"first_album\""));
        assertTrue(jsonResponse.contains("\"jcr:created\":"));
        assertTrue(jsonResponse.contains("\"created\":"));

    }

    @Test
    public void testVideoHasCreatedField_WhenJcrCreatedNotPresent() throws Exception
    {
        uploadNewVideo("second_video_created", "first_album", false);

        Thread.sleep(1000);

        getRequestExecutor().execute(
                        getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/second_video_created.json")
                ).assertContentContains("\"created\":");

        String jsonResponse = getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/second_video_created.json")
        ).getContent();

        assertTrue(jsonResponse.contains("\"title\":\"second_video_created\""));
        assertTrue(jsonResponse.contains("\"album\":\"first_album\""));
        assertTrue(jsonResponse.contains("\"created\":"));

    }

    private void uploadNewVideo(String videoTitle, String videoAlbum, Boolean includeJcrCreated) throws IOException
    {
        final MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test description"));
        entity.addPart("title", new StringBody(videoTitle));
        entity.addPart("album", new StringBody(videoAlbum));
        if (includeJcrCreated == true)
        {
            entity.addPart("jcr:created", new StringBody(""));
        }
        entity.addPart("mediaFile",
                new InputStreamBody(getClass().getResourceAsStream("/videos/video1.mp4"), "video1.mp4"));

        getRequestExecutor().execute(
                getRequestBuilder().buildPostRequest(demoChannelPath + "/vod/2012/9/1/")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);
    }
}
