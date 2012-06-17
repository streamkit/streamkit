package org.mediacenter.content.post.processor;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Called during DELETE operations, this processor is removing the files stored on hdd
 */

@Component(immediate=true, metatype=false, name="org.mediacenter.content.post.processor.DeleteVodContentProcessor")
@Service(SlingPostProcessor.class)
@Properties({
        @Property(name="service.description", value="Mediacenter - DeletVod post processor"),
        @Property(name="sling.servlet.resourceTypes", value= {"mediacenter:vod"}),
        @Property(name="sling.post.processor", value="DELETE")
})
public class DeleteVodContentProcessor extends AbstractPostProcessor implements SlingPostProcessor {
    private static final String JCR_MEDIA_PATH_CONFIG = "config/storage/servers";
    private static String MEDIA_ABSOLUTE_PATH = null;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    private Session session;

    @Reference
    private SlingRepository repository;

    @Override
    protected void doActivate(ComponentContext context)  {
        try {
            session = repository.loginAdministrative(null);
            Node storageServersNode = session.getRootNode().getNode(JCR_MEDIA_PATH_CONFIG);
            MEDIA_ABSOLUTE_PATH = storageServersNode.getProperty("rootPath").getValue().getString();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void doDeactivate(ComponentContext componentContext) {
        if (session != null) {
            session.logout();
            session = null;
        }
    }
    @Override
    protected void doProcess(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        List<String> filePaths = findFilePaths(request);
        removeFiles(filePaths);
    }

    // Search into selected node for paths to files (snapshots and videos)
    private List<String> findFilePaths(SlingHttpServletRequest request) {
        List<String> filePaths = new ArrayList<String>();
        try {
            // Session session = request.getResourceResolver().adaptTo(Session.class);
            String resourcePath = request.getResource().getPath();

            Node resourceNode = session.getRootNode().getNode(resourcePath.substring(1));
            PropertyIterator resourceNodeProperties = resourceNode.getProperties();
            
            while (resourceNodeProperties.hasNext()) {
                javax.jcr.Property pr = resourceNodeProperties.nextProperty(); 
                if (pr.getName().startsWith("snapshot")) {
                    filePaths.add(pr.getValue().getString());
                }
            }

            NodeIterator itResourceNodes = resourceNode.getNodes();
            while (itResourceNodes.hasNext()) {
                Node mediaPathNode = itResourceNodes.nextNode();
                PropertyIterator itMediaPathProperties = mediaPathNode.getProperties();

                while (itMediaPathProperties.hasNext()) {
                    javax.jcr.Property p = itMediaPathProperties.nextProperty();
                    if ("mediaPath".equals(p.getName())) {
                        filePaths.add(p.getValue().getString());
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            // throw  new Exception("Path not found exception");
        }
        return filePaths;
    }

    // Remove files
    private boolean removeFiles(List<String> filePaths) {
        boolean removedSuccessful = false;
        for (String path : filePaths) {
            String slash = (MEDIA_ABSOLUTE_PATH.endsWith("/") || path.startsWith("/")) ? "" : "/";
            File fileToDelete = new File(MEDIA_ABSOLUTE_PATH + slash + path);
            removedSuccessful = fileToDelete.delete();
        }

        return removedSuccessful;
    }

}
