package org.apache.sling.service.manifest;


import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.cdn.service.interfaces.CDNService;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Component(immediate=true)
@Service
@Properties({
        @Property(name = "service.description", value="MediaCenter FMLE profile"),
        @Property(name = "service.vendor", value="org.mediaCenter"),
        @Property(name = "sling.servlet.resourceTypes", value= "mediacenter:live"),
        @Property(name = "sling.servlet.selectors", value="player"),
        @Property(name = "sling.servlet.extensions", value="fmle"),
        @Property(name = "sling.servlet.prefix", value="-1", propertyPrivate = true)
})
public class FMLEServlet extends SlingSafeMethodsServlet  {

    @Reference
    private SlingRepository repository;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC, bind = "bindCdnService", unbind = "unbindCdnService")
    private CDNService cdnService;

    private Session session;

    SimpleDateFormat sdf = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

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

        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Disposition","attachment; filename=\"" + "FLashMediaLiverEncoder_profile.xml\"");//fileName);

        PrintWriter out = null;
        try {
            String resourcePath = "/libs/mediacenter/broadcaster/fmle_profile.xml";
            ResourceResolver resourceResolver = request.getResourceResolver();
            // req is the SlingHttpServletRequest
            Resource res = resourceResolver.getResource(resourcePath);

            InputStream inpstr = res.adaptTo(InputStream.class);

            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inpstr));

                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                // inpstr.close();
            }

            out = response.getWriter();

            Manifest manifest = new Manifest(cdnService, session, request).getManifest();
            Node originServer = session.getRootNode().getNode("config/streaming/servers");
            String originServerPath = originServer.getProperty("originServer").getValue().getString();
            String originServerLiveApp = (originServer.hasProperty("liveApplicationName")) ? originServer.getProperty("liveApplicationName").getValue().getString() : "live";


            String strProfileTemplate = writer.toString();
            String dynProfile = strProfileTemplate.replace(uStr("[url]"), uStr("rtmp://" + originServerPath + "/" + originServerLiveApp));
            
            String streamName = "";
            String videoOutputsize = "";
            String videoDatarate = "";
            String audioDatarate = "";
            String audioFormat = "MP3";
            for (Manifest.Media media : manifest.getMedias()) {
                streamName += media.getMediaURL() + ";";
                if (media.getWidth() != null && media.getHeight() != null) {
                    videoOutputsize += media.getWidth().toString() + "x" + media.getHeight().toString() + ";";
                }
                videoDatarate += media.getVideoBitrate().toString() + ";";
                audioDatarate = media.getAudioBitrate().toString();
            }

            dynProfile = dynProfile.replace(uStr("[stream]"), uStr(streamName));
            dynProfile = dynProfile.replace(uStr("[videoDatarate]"), uStr(videoDatarate));
            dynProfile = dynProfile.replace(uStr("[videoOutputsize]"), uStr(videoOutputsize));
            dynProfile = dynProfile.replace(uStr("[audioFormat]"), uStr(audioFormat));
            dynProfile = dynProfile.replace(uStr("[audioDatarate]"), uStr(audioDatarate));

            out.println(dynProfile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }
    
    private String uStr(String value) {
        return value.replace("", "\u0000");
    }



}

