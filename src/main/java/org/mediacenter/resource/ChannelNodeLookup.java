package org.mediacenter.resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.jcr.resource.JcrResourceConstants;

/**
 * Utility class for channels to find in path: channel or album
 */
public class ChannelNodeLookup
{
    public ChannelNodeLookup()
    {
    }


    /**
     * Looks in the path from RIGHT to LEFT,
     * in the search for a Node having a <code>sling:resourceType = "mediacenter:channel"</code>.
     * <p/>
     * The first node of this type to be found is returned.
     *
     * @param currentNode The Node to start the search from
     *
     * @return Either an instance of javax.jcr.Node, either null.
     *
     * @throws javax.jcr.RepositoryException
     */
    public static Node getClosestChannelInNode(Node currentNode) throws RepositoryException
    {
        if ( currentNode == null )
        {
            return null;
        }
        return findClosestNodeWithResource(currentNode, MediaCenterResourceType.CHANNEL);
    }

    /**
     * Looks in the path from RIGHT to LEFT,
     * in the search for a Node having a <code>sling:resourceType = "mediacenter:album"</code>.
     * <p/>
     * The first node of this type to be found is returned.
     *
     * @param currentNode The Node to start the search from
     *
     * @return Either an instance of javax.jcr.Node, either null.
     *
     * @throws javax.jcr.RepositoryException
     */
    public static Node getClosestAlbumInPath( Node currentNode ) throws RepositoryException
    {
        if ( currentNode == null )
        {
            return null;
        }
        return findClosestNodeWithResource(currentNode, MediaCenterResourceType.ALBUM);
    }

    private static Node findClosestNodeWithResource(Node currentNode, String resourceType) throws RepositoryException
    {
        Node node = currentNode;
        while (node != null)
        {
            String propName = JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
            if (node.hasProperty(propName) &&
                    resourceType.equals(node.getProperty(propName).getString()))
            {
                return node;
            }
            node = node.getParent();
            if ( node.getPath().equals("/") ) {
                node = null;
                break;
            }
        }
        return null;
    }


}