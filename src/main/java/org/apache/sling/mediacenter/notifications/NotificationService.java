package org.apache.sling.mediacenter.notifications;


import org.apache.felix.scr.annotations.*;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.JobProcessor;
import org.apache.sling.mediacenter.notifications.components.SendMail;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @author Cosmin Stanciu
 */
@Component(immediate=true)
@Properties({
   @Property(name="service.description", value="Mediacenter - Notification service"),
   @Property(name="event.topics", value= "org/apache/sling/mediacenter/resouces/notification/EMAIL")
})
@Service

public class NotificationService implements EventHandler, JobProcessor {

    @Reference
    private SendMail sendMail;

	public void handleEvent(Event event) {
	    if (EventUtil.isLocal(event)) {
	        EventUtil.processJob(event, this);
	    }
	}

    public boolean process(Event event) {
        String title = (String)event.getProperty("TITLE");
        String content = (String)event.getProperty("CONTENT");
        String to = (String)event.getProperty("TO");

        return sendMail.send(title, content, to);
    }
}
