package org.streamkit.vod.album;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.mediacenter.resource.ChannelNodeLookup;
import org.mediacenter.resource.MediaCenterResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamkit.vod.AlbumService;

/** Service used to manage albums */
@Component(immediate = true, metatype = false)
@Properties({
        @Property(name = "service.description",
                value = "Service to add/remove/edit albums or edit albums content")
})
@Service(value = { AlbumService.class })
public class AlbumServiceImpl implements AlbumService
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void addVideoToAlbum(Node videoNode, String albumName, Node channelNode)
            throws RepositoryException
    {

        // TBD: should we validate if the videoNode is of mediacenter:vod resourceType ?
        // TBD: should we validate if channelNode is of mediacenter:channel resourceType ?

        if ("".equals(albumName) || albumName == null)
        {
            // Find references to this video and remove it from all user-defined albums.
            // At the moment there is a 1:1 link between a video and an album.
            // A video can belong to a single user-defined album at a time.
            // This may change in the future.
            // removeNodeShares(videoNode);
            logger.info("addVideoToAlbum{} - (" + albumName + ") albumName not valid");
            return;
        }

        //0. make sure the videoNode is shareable
        videoNode.addMixin("mix:shareable");

        //1. make sure the JCR Node for the album exists
        Node albumNode = getOrCreateAlbumNode(channelNode, albumName);
        Node videoDestNode = getOrCreateDatePathInAlbum(albumNode, videoNode);

        if (videoDestNode.hasNode(videoNode.getName()))
        {
            return;
        }

        //2. add video to the album
        Session session = videoNode.getSession();
        Workspace workspace = session.getWorkspace();
        //make sure the album node is saved
        session.save();

        workspace.clone(workspace.getName(),
                videoNode.getPath(),
                videoDestNode.getPath() + "/" + videoNode.getName(),
                false);
    }


    public void addVideoToAlbum(Node videoNode, String albumName)
            throws PathNotFoundException, RepositoryException
    {
        Node channelNode = ChannelNodeLookup.getClosestChannelInNode(videoNode);

        if (channelNode == null)
        {
            throw new IllegalArgumentException("There is no mediacenter:channel in path [" + videoNode.getPath() + "]");
        }

        addVideoToAlbum(videoNode, albumName, channelNode);
    }

    public void removeVideoFromAlbum(Node videoNode, String albumPath)
            throws RepositoryException, IllegalArgumentException
    {
        if (videoNode == null)
        {
            throw new IllegalArgumentException("You must provide a videoNode");
        }
        if (albumPath == null)
        {
            logger.info("could not remove:{} from a null album", videoNode.getPath());
            return;
        }
        Boolean removed = false;
        String nodeUUID = videoNode.getIdentifier();
        if (nodeUUID != null)
        {
            NodeIterator nodes = videoNode.getSharedSet();
            while (nodes.hasNext())
            {
                Node n = nodes.nextNode();
                String nodePath = n.getPath();
                if (nodePath.contains(albumPath))
                {
                    logger.info("removed " + videoNode.getPath() + ", from " + albumPath);
                    n.remove();
                    removed = true;
                    break;
                }
            }
        }
        if (!removed)
        {
            logger.info("could not remove:{} from album:{}", videoNode.getPath(), albumPath);
        }
    }


    public void removeVideoFromOtherAlbums(Node videoNode, List<String> albumWhiteList)
            throws RepositoryException, IllegalArgumentException
    {
        if (videoNode == null)
        {
            throw new IllegalArgumentException("You must provide a videoNode");
        }
        if (albumWhiteList == null)
        {
            logger.info("could not remove:{} from a null album", videoNode.getPath());
            return;
        }

        String nodeUUID = videoNode.getIdentifier();
        if (nodeUUID != null)
        {
            NodeIterator nodes = videoNode.getSharedSet();
            while (nodes.hasNext())
            {
                Node n = nodes.nextNode();
                String nodePath = n.getPath();
                Node albumNode = ChannelNodeLookup.getClosestAlbumInPath(n);
                if ( albumNode == null ) {
                    continue;
                }
                if ( albumWhiteList.contains( albumNode.getName())) {
                    continue;
                }

                logger.info("removed " + videoNode.getPath() + ", from " + albumNode.getPath());
                n.remove();
            }
        }
    }

    private Node getOrCreateAlbumNode(Node channelNode, String albumName) throws RepositoryException
    {
        if (!channelNode.hasNode("albums"))
        {
            channelNode.addNode("albums");
        }

        String relPath = "albums/" + albumName;
        Node albumNode;
        if (!channelNode.hasNode(relPath))
        {
            albumNode = channelNode.addNode(relPath);
            albumNode.setProperty("sling:resourceType", MediaCenterResourceType.ALBUM);
            albumNode.setProperty("title", albumName);
        }
        albumNode = channelNode.getNode(relPath);
        return albumNode;
    }

    /**
     * NOTE: THIS METHOD ASSUMES ALL videos have YEAR/MONTH in path
     *
     * @param albumNode Node of the album
     * @param videoNode Node of the video
     *
     * @return the node that contains the same YEAR and MONTH of the video
     *
     * @throws RepositoryException
     */
    private Node getOrCreateDatePathInAlbum(Node albumNode, Node videoNode) throws RepositoryException
    {
        String month = videoNode.getParent().getName();
        String year = videoNode.getParent().getParent().getName();

        if (!albumNode.hasNode(year))
        {
            albumNode.addNode(year);
        }

        Node yearNode = albumNode.getNode(year);
        if (!yearNode.hasNode(month))
        {
            yearNode.addNode(month);
        }

        Node monthNode = yearNode.getNode(month);
        return monthNode;
    }


}
