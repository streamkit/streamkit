package org.streamkit.vod.post;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.service.component.ComponentContext;


public class VodPostProcessorBaseImpl implements VodPostProcessor
{
    protected int executionOrder = -1;



    protected void configure(Map<String, Object> configuration)
    {
        this.executionOrder = (Integer) configuration.get("order");
    }

    public Boolean processUpdated(Node videoNode)
    {
        return false;
    }

    public Boolean processAdded(Node videoNode) throws RepositoryException
    {
        return false;
    }

    public int getExecutionOrder()
    {
        return executionOrder;
    }
}
