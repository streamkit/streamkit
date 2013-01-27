package org.apache.sling.mediacenter.notifications;

import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.apache.sling.mediacenter.notifications.components.SendMail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SlingAnnotationsTestRunner.class)
public class NotificationServiceTest {

    @TestReference
    private ConfigurationAdmin configAdmin;

    @TestReference
    private BundleContext bundleContext;

    @TestReference
    private SendMail sendMail;

    @Test
    public void testConfigAdmin() throws Exception {
        boolean returnValue = sendMail.send("Title TEST","Content email TEST","noaddress@nodomain.com");
        assertTrue(returnValue);
        System.out.println("rwear");
    }


}