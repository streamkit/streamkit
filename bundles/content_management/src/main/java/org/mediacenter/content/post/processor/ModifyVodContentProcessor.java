package org.mediacenter.content.post.processor;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called during POST operations, this processor is adding a jcr:created and jcr:modified
 * to each content that is being added or modified.
 */

@Component(immediate = true, metatype = false,
        name = "org.mediacenter.content.post.processor.ModifyVodContentProcessor")
@Service(SlingPostProcessor.class)
@Properties({
        @Property(name = "service.description",
                value = "Mediacenter - ModifyVod post processor"),
        @Property(name = "sling.servlet.resourceTypes", value = { "mediacenter:vod" }),
        @Property(name = "sling.post.processor", value = "MODIFY")
})
public class ModifyVodContentProcessor extends EditVodProcessorBase implements SlingPostProcessor
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doProcess(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        super.doProcess(request, changes);

    }
}
