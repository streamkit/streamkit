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
        @Property(name = "service.description", value="MediaCenter Wirecast profile"),
        @Property(name = "service.vendor", value="org.mediaCenter"),
        @Property(name = "sling.servlet.resourceTypes", value= "mediacenter:live"),
        @Property(name = "sling.servlet.selectors", value="player"),
        @Property(name = "sling.servlet.extensions", value="wcst"),
        @Property(name = "sling.servlet.prefix", value="-1", propertyPrivate = true)
})
public class WirecastServlet extends SlingSafeMethodsServlet  {



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
        response.setHeader("Content-Disposition","attachment; filename=\"" + "Wirecast_profile.wcst\"");//fileName);

        PrintWriter out = null;
        try {
            String resourcePath = "/libs/mediacenter/broadcaster/wirecast_profile.wcst";
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
            String url = "rtmp://" + originServerPath + "/" + originServerLiveApp;
            String dynProfile = strProfileTemplate;

            String outputs = "";
            String presets = "";
            for (Manifest.Media media : manifest.getMedias()) {
                outputs += "<output output_enabled=\"1\" output_type=\"2\" output_transport=\"7\" output_url=\"" + url + "\" output_location=\"" + media.getMediaURL() +"\" output_presetname=\"" + media.getMediaURL() + "\" unique_id=\"1\" impersonate_fme_2_5=\"0\" output_branding=\"\" flash_user_agent=\"Wirecast/FM 1.0\" />";
                presets += "<preset output_presetname=\"" + media.getMediaURL() + "\">\n" +
                                "<movie format=\"Flash\">\n" +
                                    "<track mo_width=\"" + media.getWidth().toString() + "\" mo_height=\"" +  media.getHeight().toString() + "\" type=\"1986618479\" colourspace=\"846624121\" mo_enabled=\"1\" " +
                                    "codecname=\"H.264\" fps=\"25\" compressed=\"1\" timescale=\"3000\" " +
                                    "target_bit_rate=\"" + media.getVideoBitrate().toString() + "\" profile=\"1835100526\" limit_bit_rate=\"0\" key_frame_rate=\"240\" timecode_interval=\"30\" timecode_on=\"0\" FlashVideoDisableH264BFrames=\"0\" main_concept=\"1\" />\n" +
                                    "<track type=\"1635083375\" bps=\"16\" channels=\"2\" samplerate=\"44100\" mo_enabled=\"1\" codecname=\"MPEG-4 Audio\" target_bit_rate=\"" + media.getAudioBitrate().toString() + "\" timescale=\"44100\" />\n" +
                                "</movie>\n" +
                            "</preset>";
            }

            dynProfile = dynProfile.replace("[outputs]", outputs);
            dynProfile = dynProfile.replace("[presets]", presets);

            out.println(dynProfile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }
}

