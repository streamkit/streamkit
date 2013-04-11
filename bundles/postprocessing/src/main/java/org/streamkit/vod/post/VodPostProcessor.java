package org.streamkit.vod.post;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Processor to be invoked when a video is created or updated in JCR.
 * User: ddragosd
 */
public interface VodPostProcessor
{
    public Boolean processUpdated(Node videoNode) throws RepositoryException;
    public Boolean processAdded(Node videoNode) throws RepositoryException;
    public int getExecutionOrder();
}
