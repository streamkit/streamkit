package org.mediacenter.content.integration;

import java.lang.Exception;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mediacenter.testing.IntegrationTest;

import static org.junit.Assert.assertFalse;

/**
 */
@Category(IntegrationTest.class)
public class CreateVodContentTest extends VodManagerIntegrationTestBase
{
    @Test
    public void testNewContentIsShareable() throws Exception {
        final MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test description"));
        entity.addPart("title", new StringBody("test_video"));
//        entity.addPart("code", new InputStreamBody(getClass().getResourceAsStream("/test.js"), "test.js"));

        getRequestExecutor().execute(
                getRequestBuilder().buildPostRequest(demoChannelPath + "/vod/2012/9/1/test_video")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);

        getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).assertContentContains("mix:shareable");
    }

    @Test
    public void testNewContent_PrivatePermissions() throws Exception {
        final MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test not public"));
        entity.addPart("title", new StringBody("test_video_private"));
        entity.addPart("active", new StringBody("false"));

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildPostRequest(demoChannelPath + "/vod/2012/9/1/test_video_private")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_private.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).assertContentContains("rep:AccessControllable");

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_private.json")
        ).assertStatus(404); // private content should not be visible
    }


    @Test
    public void testNewContent_PublicPermissions() throws Exception {
        final MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test public"));
        entity.addPart("title", new StringBody("test_video_public"));

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildPostRequest(demoChannelPath + "/vod/2012/9/1/test_video_public")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_public.json")
        ).assertStatus(200); // public content should be accessible

        String content = getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_public.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).getContent();

        assertFalse("Content should not have ACL", content.contains("rep:AccessControllable"));
    }



}
