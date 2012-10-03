package org.mediacenter.content.integration;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mediacenter.testing.IntegrationTest;

/**
 * Created by IntelliJ IDEA.
 * User: ddascal
 * Date: 10/3/12
 * Time: 8:48 AM
 * To change this template use File | Settings | File Templates.
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

}
