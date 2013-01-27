package org.apache.sling.service.postprocessing;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.service.postprocessing.exception.PostprocessingException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Stanciu
 *
 * Noticiation to be sent to channel owner
 */
public class Notification  {
    
    private EventAdmin eventAdmin;
    private SlingRepository repository;
    private Exception e;
    private String propPath;
    
    private String title;
    private String content;

    private final static Logger log = LoggerFactory.getLogger(Notification.class);
    
    public Notification(EventAdmin eventAdmin, SlingRepository repository, Exception e, String propPath) {
        this.eventAdmin = eventAdmin;
        this.repository = repository;
        this.e = e;
        this.propPath = propPath;
    }
    
    // Send notification to channel owner
    public void send() {
        log.info("Sending notification: " + propPath);

        Dictionary<String,String> properties = new Hashtable<String,String>();
        properties.put("TO", getChannelEmail());
        properties.put("TITLE", getTitle());
        properties.put("CONTENT", getContent());

        Event sendMessageEvent = new Event("org/apache/sling/mediacenter/resouces/notification/EMAIL", properties);
        eventAdmin.postEvent(sendMessageEvent);
    }

    private String getTitle() {

        // Extract resource name
        Pattern pResourceName = Pattern.compile("content/channel/[0-9a-zA-Z-_ ]*/vod/[0-9]+/[0-9]+/([0-9a-zA-Z-_ ]+)", Pattern.CASE_INSENSITIVE);
        Matcher mResourceName = pResourceName.matcher(propPath);
        String resourceName = "";
        if (mResourceName.find()) {
            resourceName = mResourceName.group(1);
        }

        if (e != null) {
            return "MediaCenter notification. Upload error on mediaFile: " + resourceName + "\n" +
                    e.getMessage();
        }
        return "MediaCenter notification. Upload success, mediaFile: " + resourceName;
    }
    
    private String getContent() {
        String output = "";
        if (e != null) {
            output = "An error occurred when trying to process the uploaded file. Please contact the administrator for support on: info@crestin.tv";
            output += e.getLocalizedMessage();

            if (e instanceof PostprocessingException) {
                PostprocessingException pex =  (PostprocessingException)e; 
                if (pex.getCode() == PostprocessingException.CODEC) {
                    output = "The uploaded file is not supported by our system. Please only upload mp4 encoded files. \n";
                    output += pex.getLogMessage();
                }
                if (pex.getCode() == PostprocessingException.COMPILING) {
                    output = "A compiling error occurred when trying to process the uploaded file. Please check the integrity of the file, it might be corrupted.";
                    output += pex.getLogMessage();
                }
            }
        }
        if (e == null) {
            output = "The media file has been successfully added to the system.  \n" +
                    "You can now embed it into your page.";

        }
        return output;
    }
    
    // Return channel email address from JCR
    private String getChannelEmail() {

        // Extract jcr channel path
        Pattern pChannel = Pattern.compile("(content/channel/[0-9a-zA-Z-_ ]*)", Pattern.CASE_INSENSITIVE);
        Matcher mChannel = pChannel.matcher(propPath);
        String jcrChannelPath = "";
        if (mChannel.find()) {
            jcrChannelPath = mChannel.group(1);
        }

        Session session = null;
        try {
            session = repository.loginAdministrative(null);
            Node channelNode = session.getRootNode().getNode(jcrChannelPath);
            String email = channelNode.getProperty("email").getValue().getString();
            return email;
        } catch (RepositoryException e1) {
            log.info("No channel email has been found on this JCR path: " + jcrChannelPath);
        } finally {
            if (session != null) {
                session.logout();
                session = null;
            }
        }
        return null;
    }
}
