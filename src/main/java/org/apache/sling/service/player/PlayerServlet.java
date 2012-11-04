package org.apache.sling.service.player;


import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.cdn.service.interfaces.CDNService;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import javax.jcr.*;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(immediate=true)
@Service
@Properties({
        @Property(name="service.description", value="Mediacenter - Player live servlet"),
        @Property(name="sling.servlet.resourceTypes", value= {"mediacenter:vod","mediacenter:live"}),
        @Property(name="sling.servlet.selectors", value= "player"),
        @Property(name="sling.servlet.extensions", value= "json")
})
public class PlayerServlet extends SlingSafeMethodsServlet  {

    private final String CDN_SERVERS_PATH = "config/streaming/servers";
    private final String HTTP_SERVERS_PATH = "config/http/server";

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC, bind = "bindCdnService", unbind = "unbindCdnService")
    private CDNService cdnService;

    @Reference
    private SlingRepository repository;

    private Session session;

    protected void activate(ComponentContext componentContext)  throws Exception {
        session = repository.loginAdministrative(null);
    }
    protected void deactivate(ComponentContext componentContext) throws RepositoryException {
        if (session != null) {
            session.logout();
            session = null;
        }
    }

    protected void bindCdnService(CDNService cdnService) {
        this.cdnService = cdnService;
    }

    protected void unbindCdnService(ServiceReference serviceReference) {
        this.cdnService = null;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        Resource resource = request.getResource();

        if (ResourceUtil.isNonExistingResource(resource)) {
            throw new ResourceNotFoundException("No data to render.");
        }

        // Display streamingServers property array
        Boolean isVodResource = "mediacenter:vod".equals(resource.getResourceType());


        // Read HTTP path server information
        String httpUrl = "";
        try {
            Node httpServerNode = session.getRootNode().getNode(HTTP_SERVERS_PATH);
            httpUrl = httpServerNode.getProperty("httpUrl").getValue().getString();
            if (!httpUrl.startsWith("http://")) {
                httpUrl = "http://" + httpUrl;
            }

            if (httpUrl.endsWith("/")) {
                httpUrl.substring(0, httpUrl.length() -1);
            }

        } catch (RepositoryException e) {
            throw new ServletException("No https servers have been found in the system. Please provide at least one..");
        }

        // Output content node and available streaming servers in JSON form
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            Node resourceNode = session.getRootNode().getNode(resource.getPath().substring(1));

            final JSONWriter w = new JSONWriter(response.getWriter());
            w.setTidy(true);

            // Display resource properties
            w.object();


            // TODO: simplify this code
            // 1. use a specialized Sling class to write a node to JSON

            PropertyIterator itResourceNodeProperties = resourceNode.getProperties();
            while(itResourceNodeProperties.hasNext()) {
                javax.jcr.Property property = itResourceNodeProperties.nextProperty();

                if ( property.isMultiple() ) { // fix for mix:shareable
                    continue;
                }

                String propertyName = property.getName();
                String propertyValue = property.getValue().getString();
                if ("snapshotPath".equals(propertyName)) {
                    propertyValue = httpUrl + propertyValue;
                }
                w.key(propertyName).value(propertyValue);
            }


            // Display mediaPath property array
            w.key("mediaPaths");
            w.array();

            NodeIterator itResourceNodes = resourceNode.getNodes();
            while (itResourceNodes.hasNext()) {
                Node mediaPathNode = itResourceNodes.nextNode();
                PropertyIterator itMediaPathProperties = mediaPathNode.getProperties();
                w.object();
                while (itMediaPathProperties.hasNext()) {
                    javax.jcr.Property p = itMediaPathProperties.nextProperty();

                    // fix for CMS-48
                    if ("jcr:data".equals(p.getName()) ) {
                        continue;
                    }

                    String value = p.getValue().getString();

                    // Build absolute path to Wowza media files
                    if ("mediaPath".equals(p.getName())) {
                        String mediaHttpUrl = httpUrl.replace("http://", "http/");
                        // Media path should be with prefix for VOD and only the streamname for LIVE
                        String mediaPath = (isVodResource) ? mediaHttpUrl + "/" + value : value;
                        w.key(p.getName()).value(mediaPath);

                        // Build absolute download path to media file
                        String downloadPath = httpUrl + "/" + value;
                        w.key("downloadPath").value(downloadPath);

                        if (cdnService != null) {
                            w.key("connectionCounts").value(cdnService.connectionCounts(value));
                        }
                        continue;
                    }

                    w.key(p.getName()).value(value);
                }
                w.endObject();
            }
            w.endArray();

            w.endObject();

        } catch(JSONException je) {
            throw (IOException)new IOException("JSONException in doGet").initCause(je);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
}