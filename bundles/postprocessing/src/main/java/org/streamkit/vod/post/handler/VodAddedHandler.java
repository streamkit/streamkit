package org.streamkit.vod.post.handler;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobProcessor;
import org.mediacenter.resource.MediaCenterResourceTopic;
import org.mediacenter.resource.MediaCenterResourceType;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

import org.apache.sling.event.jobs.JobUtil;

/**
 * Created by IntelliJ IDEA.
 * User: ddascal
 * Date: 11/14/12
 * Time: 9:43 AM
 * To change this template use File | Settings | File Templates.
 */
@Component(immediate = true)
@Properties({
        @org.apache.felix.scr.annotations.Property(name = "service.description",
                value = "Event handler for add/edit video"),
        @org.apache.felix.scr.annotations.Property(name = "event.topics",
                value = org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_ADDED)
})
@Service( value={EventHandler.class})
public class VodAddedHandler implements EventHandler, JobProcessor
{
    @Reference
    private LogService logger;

    @Reference
    private EventAdmin eventAdmin;

    public void handleEvent(Event event)
    {
        String resourceType = (String) event.getProperty(SlingConstants.PROPERTY_RESOURCE_TYPE);

        if ( MediaCenterResourceType.VOD.equals(resourceType)) {
            logger.log(LogService.LOG_INFO, "process {}");
            if (EventUtil.isLocal(event)) {
                JobUtil.processJob( event, this);
            }
        }
    }

    public boolean process(Event event)
    {
        String path = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
        String resourceType = (String) event.getProperty(SlingConstants.PROPERTY_RESOURCE_TYPE);

        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(JobUtil.PROPERTY_JOB_TOPIC, MediaCenterResourceTopic.VOD_ADDED_TOPIC);

        props.put("resourcePath", path);
        // When a job event (event with the topic =org/apache/sling/event/job=) is received,
        // a new event with the topic from the property =event.job.topic= is fired.
        final Event myEvent = new Event(JobUtil.TOPIC_JOB, props);
        // sending the event asyncronously
        logger.log(LogService.LOG_INFO, "sending async event on path=" + path + ",resourceType=" + resourceType);
        eventAdmin.postEvent( myEvent );
        return true;
    }


}
