package org.streamkit.vod.post;

import javax.jcr.Node;

/**
 * Processor to be invoked when a video is created or updated in JCR.
 * User: ddragosd
 */
public interface VodPostProcessor
{
    Boolean process(Node videoNode);
}
