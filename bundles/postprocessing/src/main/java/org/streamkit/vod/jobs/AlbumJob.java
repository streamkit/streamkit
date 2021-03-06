package org.streamkit.vod.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.JobProcessor;
import org.apache.sling.event.jobs.JobUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.mediacenter.resource.ChannelNodeLookup;
import org.mediacenter.resource.MediaCenterResourceTopic;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamkit.vod.AlbumService;

/**
 * This job adds a specific video to an album, based on the property saved on the JCR Node
 * when the resource has been saved.
 */
@Deprecated
public class AlbumJob
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private JobManager jobManager;

    @Reference
    private AlbumService albumService;

    @Reference
    private SlingRepository repository;

    /**
     * When the event handler receives a job event, it calls =Job.processJob(event, this)= and returns.
     * The =process(Event)= method is now called <b>in the backgorund</b> and when it finishes, the job event handler
     * is notified that the job is completed.
     *
     * @param event
     */
    public void handleEvent(Event event)
    {
        /*logger.info("handleEvent{}");
        if ( !JobUtil.acknowledgeJob(event) )
        {
            JobUtil.rescheduleJob(event);
        }
        if (EventUtil.isLocal(event))
        {
            JobUtil.processJob(event, this);
        }*/
    }


    /**
     * To understand this method better read the specs at
     * http://sling.apache.org/site/eventing-and-jobs.html
     * <p/>
     * The event handler must implement the =JobProcessor= interface which consists of a single =process(Event)= method.
     * When the event handler receives a job event, it calls =Job.processJob(event, this)= and returns.
     * The =process(Event)= method is now called <b>in the background</b> and when it finishes, the job event handler
     * is notified that the job is completed.
     * <p/>
     * If the event handler wants to do the background processing by itself or does not need background processing
     * at all, it must signal completion of the job by calling =JobUtil.finishedJob(event)=.
     *
     * @param event
     *
     * @return
     */
    public boolean process(Event event)
    {
        logger.info("process: {}", event);
        String resourcePath = event.getProperty("resourcePath").toString();
        Session session = null;

        try
        {
            session = repository.loginAdministrative(null);
            updateAlbumForVideo(resourcePath, session);
        }
        catch (RepositoryException e)
        {
            logger.error("Could not access repository. ", e);
        }
        finally
        {
            session.logout();
        }

        signalComplete(event);
        return true;
    }

    private void updateAlbumForVideo(String path, Session session) throws RepositoryException
    {
        try
        {
            // NODE: this will get called when the video is added to the album too.
            Node videoNode = session.getNode(path);
            Node existingAlbum = ChannelNodeLookup.getClosestAlbumInPath(videoNode);

            if (existingAlbum != null)
            {
                return;
            }

            List<String> albumList = new ArrayList<String>();

            if (videoNode.hasProperty("album"))
            {
                javax.jcr.Property album = videoNode.getProperty("album");
                String albumString = album.getString();
                albumList = Arrays.asList( albumString.replaceAll(" ", "").split(","));

                for ( String albumName: albumList ) {
                    albumService.addVideoToAlbum(videoNode, albumName );
                }
            }

            //remove video from other albums
            Boolean mustSave = albumService.removeVideoFromOtherAlbums( videoNode, albumList );
            if ( mustSave == true )
            {
                session.save();
            }
        }
        catch (RepositoryException e)
        {
            logger.error("Could not add/remove video to/from album. ", e);
        }
        catch (IllegalArgumentException e)
        {
            logger.error("AlbumService exception. Could not add/remove video to/from album. ", e);
        }
    }

    /**
     * TODO: investigate what happens if there are other EventHandlers attached to the same event
     *
     * @param event
     */
    private void signalComplete(Event event)
    {
        logger.info("complete{}");
        JobUtil.finishedJob(event);
    }


}
