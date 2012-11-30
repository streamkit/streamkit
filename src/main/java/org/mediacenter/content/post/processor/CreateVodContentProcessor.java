package org.mediacenter.content.post.processor;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.http.HttpServletRequest;


import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.jcr.base.util.AccessControlUtil;
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

@Component(immediate = true, metatype = false,
        name = "org.mediacenter.content.post.processor.CreateVodContentProcessor")
@Service(SlingPostProcessor.class)
@Properties({
        @Property(name = "service.description",
                value = "Mediacenter - CreateVod post processor"),
        @Property(name = "sling.servlet.resourceTypes", value = { "mediacenter:vod" }),
        @Property(name = "sling.post.processor", value = "CREATE")
})
public class CreateVodContentProcessor extends EditVodProcessorBase implements SlingPostProcessor
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doProcess(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        super.doProcess(request, changes);
        validateLocation(request, changes);
    }

    /**
     * Method used to handle international characters and also to replace spaces with "_".
     * -------------------
     * NOTE : IN PROGRESS
     * -------------------
     * @param request
     * @param changes
     * @throws Exception
     */
    private void validateLocation(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        Session session = request.getResourceResolver().adaptTo(Session.class);

        Node n = session.getNode(request.getResource().getPath());
        if ( ! n.hasProperty("title")) {
            return;
        }
        String name = n.getProperty("title").getString();

        String normalizedName = getNormalizedName( name );

        // to learn more about nameHint go to this URL
        // http://sling.apache.org/site/manipulating-content-the-slingpostservlet-servletspost.html
        n.setProperty("title", getNormalizedName( name ));
    }

    private String getNormalizedName( String name ) {
        String nfdNormalizedString = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}
