package org.apache.sling.service.player;

import org.apache.sling.api.SlingHttpServletRequest;

import javax.jcr.Node;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Cosmin Stanciu
 */
public class SocialMediaMetadata {
    
    private final static String HTTP_JCR_PATH = "config/http/server";

    public static final String getVideoPath(SlingHttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestPath = request.getRequestURL().toString();
        String f4m_manifest = requestPath.replace(".fplayer", ".player").replace(".html", ".f4m");

        Node mediaNode = request.getResource().adaptTo(Node.class);

        String absoluteSnapshotPath = "";


        String videoPath = "http://watt.at/clientStrobe/StrobeMediaPlayback.swf?" +
                "plugin_cdn=http://watt.at/clientStrobe/StrobeCDNPlugin.swf" + "&" +
                "src=" + f4m_manifest + "&" +
                "poster=" + getScreenshot(request, response) + "&" +
                "autoPlay=true" + "&" +
                "controlBarAutoHide=true" + "&" +
                "controlBarPosition=bottom" + "&" +
                "streamType=" + isLive(mediaNode) + "&" +
                "initialBufferTime=2" + "&" +
                "expandedBufferTime=7";

        return videoPath;
    }

    public static final String getThumbnail(SlingHttpServletRequest request, HttpServletResponse response) throws Exception {

        Node mediaNode = request.getResource().adaptTo(Node.class);

        String absoluteSnapshotPath = "http://watt.at/clientStrobe/livestream_thumbnail.jpg";
        if (mediaNode.hasProperty("snapshotPath")) {
            String snapshot = mediaNode.getProperty("snapshotPath").getString();

            Node httpPathNode = mediaNode.getSession().getRootNode().getNode(HTTP_JCR_PATH);
            String httpUrl = httpPathNode.getProperty("httpUrl").getString();

            absoluteSnapshotPath = httpUrl + "/" + snapshot;
        }

        return absoluteSnapshotPath;
    }

    private static final String getScreenshot(SlingHttpServletRequest request, HttpServletResponse response) throws Exception {

        Node mediaNode = request.getResource().adaptTo(Node.class);

        String absoluteSnapshotPath = "http://watt.at/clientStrobe/livebroadcast_starting_shortly.jpg";
        if (mediaNode.hasProperty("snapshotPath")) {
            String snapshot = mediaNode.getProperty("snapshotPath").getString();

            Node httpPathNode = mediaNode.getSession().getRootNode().getNode(HTTP_JCR_PATH);
            String httpUrl = httpPathNode.getProperty("httpUrl").getString();

            absoluteSnapshotPath = httpUrl + "/" + snapshot;
        }

        return absoluteSnapshotPath;
    }

    public static final String getTitle(SlingHttpServletRequest request, HttpServletResponse response) throws Exception {

        Node mediaNode = request.getResource().adaptTo(Node.class);

        String title = "Livestream";
        if (mediaNode.hasProperty("title")) {
            title = mediaNode.getProperty("title").getString();
        }

        return title;
    }

    public static final String getDescription(SlingHttpServletRequest request, HttpServletResponse response) throws Exception {

        Node mediaNode = request.getResource().adaptTo(Node.class);

        String description = "Live broadcast";
        if (mediaNode.hasProperty("description")) {
            description = mediaNode.getProperty("description").getString();
        }

        return description;
    }
    
    private static boolean isLive(Node mediaNode) throws Exception {
        boolean isLive = true;
        if (mediaNode.hasProperty("duration")) {
            isLive = false;
        }
        return isLive;
    }
    
}
