package org.streamkit.vod;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Service that links an existing video to an album.
 * <p/>
 * User: ddragosd
 * Date: 11/30/12
 */
public interface AlbumService
{
    /**
     * Adds an existing video to the given <code>albumName</code>.
     * If <code>albumName</code> doesn't exist in the JCR repository, a new album is created.
     *
     * @param videoNode   JCR Node with the video to be added into the album
     * @param albumName   The name of the album
     * @param channelNode The ChannelNode the video belongs to
     *
     * @throws RepositoryException
     */
    void addVideoToAlbum(Node videoNode, String albumName, Node channelNode)
            throws RepositoryException;

    /**
     * Adds an existing video to the given <code>albumName</code>.
     * If <code>albumName</code> doesn't exist in the JCR repository, a new album is created.
     * <p>
     * This method looks in the path of the video node in the search for the channel that the video belongs to.
     * The first Node of <code>sling:resourceType="mediacenter:channel" </code> is used.
     * If channels are nested ( channel with children channels ) , the selected channel is last one in the URL.
     * </p>
     *
     * @param videoNode JCR Node with the video to be added into the album
     * @param albumName The name of the album
     *
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    void addVideoToAlbum(Node videoNode, String albumName)
            throws PathNotFoundException, RepositoryException;

    /**
     * Removes the corresponding video link from the album.
     * If the no video is found in that album, this method doesn't throw any exception. It won't do anything in that case.
     *
     * @param videoNode The VideoNode to be removed.
     * @param albumPath The JCR path to the album
     *
     * @throws RepositoryException
     * @throws IllegalArgumentException
     */
    void removeVideoFromAlbum(Node videoNode, String albumPath) throws RepositoryException, IllegalArgumentException;

    /**
     * Removes the video from all other albums, except the allowed <code>albumWhiteList</code>
     *
     * @param videoNode      The videoNode from the library
     * @param albumWhiteList The list of albums that should contain this album
     *                       If the video belongs to other albums, its reference is removed from those albums
     * @return true, if there are modifications to be saved, false otherwise
     */
    Boolean removeVideoFromOtherAlbums(Node videoNode, List<String> albumWhiteList)
            throws RepositoryException, IllegalArgumentException;
}
