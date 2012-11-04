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
        @Property(name = "sling.servlet.extensions", value="f4m"),
        @Property(name = "sling.servlet.prefix", value="-1", propertyPrivate = true)
})
public class F4MServlet extends SlingSafeMethodsServlet  {



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

            out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            out.println("<manifest xmlns=\"http://ns.adobe.com/f4m/1.0\">");
            out.println("<id>" + manifest.getId() + "</id>");
            out.println("<mimeType>video/mp4</mimeType>");
            out.println("<streamType>" + manifest.getStreamType() + "</streamType>");
            out.println("<deliveryType>streaming</deliveryType>");
            out.println("<baseURL>rtmp://" + manifest.getBaseURL() + "</baseURL>");
            for (Manifest.Media media : manifest.getMedias()) {
                String mediaParams ="<media url=\"" + media.getMediaURL() + "\" ";
                if (media.getVideoBitrate() != null && media.getAudioBitrate() != null) {
                    Long bitrate = media.getAudioBitrate() + media.getVideoBitrate();
                    mediaParams += "bitrate=\"" + bitrate + "\" ";
                }
                if (media.getWidth() != null) mediaParams += "width=\"" + media.getWidth() + "\" ";
                if (media.getHeight() != null) mediaParams += "height=\"" + media.getHeight() + "\" ";
                mediaParams +=  "/>";        

                out.println(mediaParams);
            }
            out.println("</manifest>");

        } catch (Exception e)  {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }


    }



}

