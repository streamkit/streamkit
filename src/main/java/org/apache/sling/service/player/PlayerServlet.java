package org.apache.sling.service.player;


import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.apache.sling.cdn.service.interfaces.CDNServer;
import org.apache.sling.cdn.service.interfaces.CDNService;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import javax.jcr.*;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        List<CDNServer> cdnServers = new ArrayList<CDNServer>();

        // Read addresses from CDN in case plugin is installed
        if (cdnService != null) {
            // In case a mod_proxy is being used, client ip is available only in header "X-Forwarded-For"
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }

            cdnServers = cdnService.hostAddresses(ip);
        }

        // Read only one server address in case no CDN plugin is installed
        if (cdnService == null) {
            try {
                Node serversNode = session.getRootNode().getNode(CDN_SERVERS_PATH);
                for (NodeIterator ni = serversNode.getNodes(); ni.hasNext(); ) {
                    Node node = ni.nextNode();
                    String streamUrl = node.getProperty("streamUrl").getValue().getString();
                    String vodApplicationName = node.getProperty("vodApplicationName").getValue().getString();
                    String liveApplicationName = node.getProperty("liveApplicationName").getValue().getString();
                    CDNServer cdnServer = new CDNServer();
                    cdnServer.setStreamUrl(streamUrl);
                    cdnServer.setVodApplicationName(vodApplicationName);
                    cdnServer.setLiveApplicationName(liveApplicationName);
                    cdnServers.add(cdnServer);
                    break;
                }
            } catch (RepositoryException e) {
                throw new ServletException("No streaming servers have been found in the system. Please provide at least one..");
            }
        }

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
                String propertyName = property.getName();
                String propertyValue = property.getValue().getString();
                if ("snapshotPath".equals(propertyName)) {
                    propertyValue = httpUrl + "/" + propertyValue;
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
                    
                    // Build absolute path to Wowza media file
                    if ("mediaPath".equals(p.getName())) {
                        String mediaHttpUrl = httpUrl.replace("http://", "http/");
                        String mediaPath = mediaHttpUrl + "/" + value;
                        w.key(p.getName()).value(mediaPath);

                        // Build absolute download path to media file
                        String downloadPath = httpUrl + "/" + value;
                        w.key("downloadPath").value(downloadPath);
                        continue;
                    }




                    if ("mediaPath".equals(p.getName()) && cdnService != null) {
                        w.key("connectionCounts").value(cdnService.connectionCounts(value));
                    }
                    w.key(p.getName()).value(value);
                }
                w.endObject();
            }
            w.endArray();

            // Display streamingServers property array
            Boolean isVodResource = "mediacenter:vod".equals(resource.getResourceType());
            w.key("streamingServers");
            w.array();
            for (CDNServer cdnServer : cdnServers) {
                w.object();
                // Display different streamUrl for vod and live
                String streamUrl = cdnServer.getStreamUrl();
                if (isVodResource) {
                    streamUrl += "/" + cdnServer.getVodApplicationName() + "/_definst_/mp4:";
                }
                else {
                    streamUrl += "/" + cdnServer.getLiveApplicationName() + "/";
                }
                w.key("streamUrl").value(streamUrl);
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

