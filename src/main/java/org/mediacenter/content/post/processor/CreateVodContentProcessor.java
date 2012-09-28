package org.mediacenter.content.post.processor;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called during POST operations, this processor is adding a jcr:created and jcr:modified
 * to each content that is being added or modified.
 */

@Component(immediate=true, metatype=false, name="org.mediacenter.content.post.processor.CreateVodContentProcessor")
@Service(SlingPostProcessor.class)
@Properties({
    @Property(name="service.description", value="Mediacenter - CreateVod post processor to add jcr:created and jcr:modified info"),
    @Property(name="sling.servlet.resourceTypes", value= {"mediacenter:vod"}),
    @Property(name="sling.post.processor", value="CREATE")
})
public class CreateVodContentProcessor extends AbstractPostProcessor implements SlingPostProcessor
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doProcess(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        logger.info("doProcess{}", request.getResource().getResourceType());

        Session session = request.getResourceResolver().adaptTo(Session.class);

        Node n = session.getNode( request.getResource().getPath() );
        NodeType[] mixins = n.getMixinNodeTypes();

        n.addMixin("mix:shareable");
//        n.addMixin("mix:created");
//        n.addMixin("mix:lastModified");
        // TODO: add ACL for this node according to "active" property
    }

}
