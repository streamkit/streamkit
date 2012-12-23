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
@Component(immediate = true, metatype = false)
@Properties({
        @Property(name = "service.description",
                value = "Job for adding a content to an album"),
        @Property(name = "event.topics",
                value = { MediaCenterResourceTopic.VOD_ADDED_TOPIC, MediaCenterResourceTopic.VOD_UPDATED_TOPIC })
})
@Service(value = { EventHandler.class })
public class AlbumJob implements EventHandler, JobProcessor
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
        logger.info("handleEvent{}");
        if (EventUtil.isLocal(event))
        {
            JobUtil.processJob(event, this);
        }
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
        logger.info("process{}");
        String resourcePath = event.getProperty("resourcePath").toString();

        try
        {
            updateAlbumForVideo(resourcePath);
        }
        catch (RepositoryException e)
        {
            logger.error("Could not access repository. ", e);
        }


        signalComplete(event);
        return true;
    }

    private void updateAlbumForVideo(String path) throws RepositoryException
    {
        Session session = repository.loginAdministrative(null);

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
            albumService.removeVideoFromOtherAlbums( videoNode, albumList );
        }
        catch (RepositoryException e)
        {
            logger.error("Could not add/remove video to/from album. ", e);
        }
        catch (IllegalArgumentException e)
        {
            logger.error("AlbumService exception. Could not add/remove video to/from album. ", e);
        }
        finally
        {
            session.save();
            session.logout();
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
