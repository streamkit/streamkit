package org.streamkit.vod.post.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.mediacenter.resource.ChannelNodeLookup;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamkit.vod.AlbumService;
import org.streamkit.vod.post.VodPostProcessor;
import org.streamkit.vod.post.VodPostProcessorBaseImpl;

/**
 * Processor ensuring the Video is added the the corresponding albums.
 * <p/>
 * User: ddragosd
 */
@Component(immediate = true, metatype = true)
@Properties({
        @Property(name = "order", intValue = 20)
})
@Service(value = { VodPostProcessor.class })
public class AlbumProcessor extends VodPostProcessorBaseImpl implements VodPostProcessor
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private AlbumService albumService;

    @Activate
    public void start(final ComponentContext context, final Map<String, Object> configuration) throws Exception
    {
        configure(configuration);
    }

    @Deactivate
    public void stop(final ComponentContext context)
    {

    }

    public Boolean processUpdated(Node videoNode)
    {
        updateAlbumForVideo(videoNode);
        return true;
    }

    public Boolean processAdded(Node videoNode) throws RepositoryException
    {
        updateAlbumForVideo(videoNode);
        return true;
    }

    private void updateAlbumForVideo(Node videoNode)
    {
        try
        {
            // NOTE: this will get called when the video is added to the album too.
            Session session = videoNode.getSession();
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
                albumList = Arrays.asList(albumString.replaceAll(" ", "").split(","));

                for (String albumName : albumList)
                {
                    albumService.addVideoToAlbum(videoNode, albumName);
                }
            }

            //remove video from other albums
            Boolean mustSave = albumService.removeVideoFromOtherAlbums(videoNode, albumList);

            // TODO: do we need to save here or in the PostProcessorJob ?
            //if (mustSave == true)
            //{
            //    session.save();
            //}
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

}
