package org.apache.sling.service.manifest;


import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.cdn.service.interfaces.CDNService;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Component(immediate=true)
@Service
@Properties({
        @Property(name = "service.description", value="MediaCenter RSS Servlet"),
        @Property(name = "service.vendor", value="org.mediaCenter"),
        @Property(name = "sling.servlet.resourceTypes", value= {"mediacenter:vod","mediacenter:live"}),
        @Property(name = "sling.servlet.selectors", value="player"),
        @Property(name = "sling.servlet.extensions", value="m3u8"),
        @Property(name = "sling.servlet.prefix", value="-1", propertyPrivate = true)
})
public class M3U8Servlet extends SlingSafeMethodsServlet  {



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
        PrintWriter out = null;

        try {

            out = response.getWriter();

            Manifest manifest = new Manifest(cdnService, session, request).getManifest();

            out.println("#EXTM3U");
            out.println("#EXT-X-VERSION:3");
            for (Manifest.Media media : manifest.getMedias()) {
                
                String mediaParams = "#EXT-X-STREAM-INF:PROGRAM-ID=1,";
                Long bandwidth = (media.getAudioBitrate() + media.getVideoBitrate()) * 1000;
                mediaParams += "BANDWIDTH=" + bandwidth;
                if (media.getAudioCodec() != null && media.getVideoCodec() != null) mediaParams += ",CODECS=\"" + media.getAudioCodec() + "," + media.getVideoCodec() + "\"";
                if (media.getWidth() != null && media.getHeight() != null) mediaParams += ",RESOLUTION=" + media.getWidth() + "x" + media.getHeight() + "";
                out.println(mediaParams);
                long sessionNumber = (long) Math.floor(Math.random() * 900000000L) + 100000000L;
                out.println("http://" + manifest.getBaseURL() + "/" + media.getMediaURL() + "/chunklist.m3u8?wowzasessionid=" + sessionNumber);
            }

        } catch (Exception e)  {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }


    }



}

