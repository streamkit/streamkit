package org.streamkit.vod.post.processor;

import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamkit.vod.post.VodPostProcessor;

/**
 * Ensures that any Video has a dateCreated property set
 * User: ddragosd
 */
@Component(immediate = true, metatype = true)
@Properties({
        @Property(name = "order", intValue = 10)
})
@Service(value = { VodPostProcessor.class })
public class DateCreatedProcessor implements VodPostProcessor
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Boolean process(Node videoNode)
    {
        try
        {
            ensureCreatedPropertyExistsForVideo( videoNode );
        }
        catch (RepositoryException e)
        {
            return true;
        }
        return true;
    }

    private void ensureCreatedPropertyExistsForVideo(Node videoNode) throws RepositoryException
    {
        if (!videoNode.hasProperty("created"))
        {
            Calendar createdDate = null;
            if (videoNode.hasProperty("jcr:created"))
            {
                createdDate = videoNode.getProperty("jcr:created").getDate();
            }
            else
            {
                createdDate = Calendar.getInstance();
                createdDate.setTime(new Date());
            }
            videoNode.setProperty("created", createdDate);
        }
    }

}
