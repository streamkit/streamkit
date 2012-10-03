package org.mediacenter.content.integration;

import org.apache.sling.testing.tools.sling.SlingClient;
import org.apache.sling.testing.tools.sling.SlingTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.mediacenter.testing.IntegrationTest;

/**
 * Created by IntelliJ IDEA.
 * User: ddascal
 * Date: 9/30/12
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Category(IntegrationTest.class)
public class VodManagerAccessTest extends VodManagerIntegrationTestBase
{

    @Test
    public void testAccessToVodManager() throws Exception
    {
        getRequestExecutor().execute(
                getRequestBuilder().buildGetRequest(demoChannelPath + ".json")
        ).assertStatus(200);

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + ".vodManager.html")
                        .withCredentials("wrong", "wrong")
        ).assertStatus(401); // UNAUTHORIZED

        getRequestExecutor().execute(
                getRequestBuilder()
                        .buildGetRequest(demoChannelPath + ".vodManager.html")
                        .withCredentials("admin", "admin")
        ).assertStatus(200); // AUTHORIZED

    }

}

