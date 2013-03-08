package org.streamkit.vod.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.EventUtil;
import org.apache.sling.event.jobs.JobUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.mediacenter.resource.MediaCenterResourceTopic;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.apache.sling.event.jobs.JobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamkit.vod.post.VodPostProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: ddascal
 * Date: 3/3/13
 * Time: 9:50 PM
 * To change this template use File | Settings | File Templates.
 */

@Component(immediate = true, metatype = false)
@References({
        @Reference(name = "vodProcessor", referenceInterface = VodPostProcessor.class,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
})
@Properties({
        @Property(name = "service.description",
                value = "Job called for background processing post creating or updating a Video"),
        @Property(name = "event.topics",
                value = { MediaCenterResourceTopic.VOD_ADDED_TOPIC,
                        MediaCenterResourceTopic.VOD_UPDATED_TOPIC })
})
@Service(value = { EventHandler.class })
public class PostProcessorJob implements EventHandler, JobProcessor
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private SlingRepository repository;

    private final List<VodPostProcessor> postProcessors = new ArrayList<VodPostProcessor>();

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
        if (!JobUtil.acknowledgeJob(event))
        {
            JobUtil.rescheduleJob(event);
        }
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
        logger.info("process: {}", event);
        String resourcePath = event.getProperty("resourcePath").toString();
        Session session = null;

        try
        {
            session = repository.loginAdministrative(null);
            Node videoNode = session.getNode(resourcePath);

            for (VodPostProcessor processor : postProcessors)
            {
                // TODO: execute processors in order
                Boolean result = false;

                if (event.getTopic().equals(MediaCenterResourceTopic.VOD_ADDED_TOPIC))
                {
                    result = processor.processAdded(videoNode);
                }
                else
                {
                    result = processor.processUpdated(videoNode);
                }

                if (!result)
                {
                    return false;
                }
            }

            session.save();
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

    protected void bindVodProcessor(final VodPostProcessor processor)
    {
        synchronized (this.postProcessors)
        {
            postProcessors.add(processor);
            sortPostProcessors();
        }
    }

    protected void unbindVodProcessor(final VodPostProcessor processor)
    {
        synchronized (this.postProcessors)
        {
            postProcessors.remove(processor);
        }
    }

    private void sortPostProcessors()
    {
        Collections.sort(postProcessors,
                new Comparator<VodPostProcessor>()
                {
                    public int compare(VodPostProcessor vodPostProcessor, VodPostProcessor vodPostProcessor2)
                    {
                        return (vodPostProcessor.getExecutionOrder() - vodPostProcessor2.getExecutionOrder());
                    }
                });
    }

    private void signalComplete(Event event)
    {
        logger.info("complete{}");
        JobUtil.finishedJob(event);
    }
}
