package org.mediacenter.content.integration;


import java.io.IOException;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mediacenter.testing.IntegrationTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class EditVodContentTest extends VodManagerIntegrationTestBase
{
    @Test
    public void testEditingContent_Permissions() throws Exception {
        MultipartEntity entity = new MultipartEntity();
        // Add Sling POST options
        entity.addPart("sling:resourceType", new StringBody("mediacenter:vod"));
        entity.addPart("description", new StringBody("test not public"));
        entity.addPart("active", new StringBody("false"));

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildPostRequest(demoChannelPath + "/vod/2012/9/1/test_video_private")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(201);

        // MAKE VIDEO PUBLIC

        entity = new MultipartEntity();
        entity.addPart("description", new StringBody("PUBLIC"));
        entity.addPart("active", new StringBody("true"));
        updateVod(entity);

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_private.json")
        ).assertStatus(200); // video should be accessible

        String content = getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_private.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).getContent();

        assertFalse("Content should not have ACL", content.contains("rep:AccessControllable"));

        // MAKE VIDEO PRIVATE
        entity = new MultipartEntity();
        entity.addPart("description", new StringBody("PRIVATE"));
        entity.addPart("active", new StringBody("false"));
        updateVod(entity);

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + "/vod/2012/9/1/test_video_private.json")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
        ).assertContentContains("rep:AccessControllable");

    }

    private void updateVod(MultipartEntity entity) throws IOException
    {
        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildPostRequest(demoChannelPath + "/vod/2012/9/1/test_video_private")
                        .withCredentials(SlingTestBase.ADMIN, SlingTestBase.ADMIN)
                        .withEntity(entity)
        ).assertStatus(200);
    }

}
