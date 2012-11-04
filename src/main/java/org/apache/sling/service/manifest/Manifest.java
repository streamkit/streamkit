package org.apache.sling.service.manifest;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.cdn.service.interfaces.CDNServer;
import org.apache.sling.cdn.service.interfaces.CDNService;

import javax.jcr.*;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Stanciu
 */
public class Manifest {
    private String id;
    private String baseURL;
    private String streamType;
    private List<Media> medias = new ArrayList<Media>();

    private CDNService cdnService;
    private Session session;
    private SlingHttpServletRequest request;

    private final String CDN_SERVERS_PATH = "config/streaming/servers";
    private final String HTTP_SERVERS_PATH = "config/http/server";
    
    public Manifest () {}
    
    public Manifest (CDNService cdnService, Session session, SlingHttpServletRequest request) {
        this.cdnService = cdnService;
        this.session = session;
        this.request = request;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public List<Media> getMedias() {
        return medias;
    }

    public void setMedias(List<Media> medias) {
        this.medias = medias;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public class Media {
        private String mediaURL;
        private Long audioBitrate;
        private Long videoBitrate;
        private Long width;
        private Long height;
        private String videoCodec;
        private String audioCodec;

        public Media () {};
        
        public Media (String mediaURL, Long audioBitrate, Long videoBitrate, Long width, Long height, String videoCodec, String audioCodec) {
            this.mediaURL = mediaURL;
            this.audioBitrate = audioBitrate;
            this.videoBitrate = videoBitrate;
            this.width = width;
            this.height = height;
            this.videoCodec = videoCodec;
            this.audioCodec = audioCodec;
        }

        public String getMediaURL() {
            return mediaURL;
        }

        public void setMediaURL(String mediaURL) {
            this.mediaURL = mediaURL;
        }

        public Long getAudioBitrate() {
            return audioBitrate;
        }

        public void setAudioBitrate(Long audioBitrate) {
            this.audioBitrate = audioBitrate;
        }

        public Long getVideoBitrate() {
            return videoBitrate;
        }

        public void setVideoBitrate(Long videoBitrate) {
            this.videoBitrate = videoBitrate;
        }

        public Long getWidth() {
            return width;
        }

        public void setWidth(Long width) {
            this.width = width;
        }

        public Long getHeight() {
            return height;
        }

        public void setHeight(Long height) {
            this.height = height;
        }

        public String getVideoCodec() {
            return videoCodec;
        }

        public void setVideoCodec(String videoCodec) {
            this.videoCodec = videoCodec;
        }

        public String getAudioCodec() {
            return audioCodec;
        }

        public void setAudioCodec(String audioCodec) {
            this.audioCodec = audioCodec;
        }
    }
    
    
    public Manifest getManifest () throws Exception {

        Manifest manifest = new Manifest();
        
        Resource resource = request.getResource();

        if (ResourceUtil.isNonExistingResource(resource)) {
            throw new ResourceNotFoundException("No data to render.");
        }
        // Display streamingServers property array
        Boolean isVodResource = "mediacenter:vod".equals(resource.getResourceType());

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

        // Return only the first server from list.
        CDNServer server = null;
        for (CDNServer cdnServer : cdnServers) {
            server = cdnServer;
            break;
        }

        String appName = (isVodResource) ? server.getVodApplicationName() : server.getLiveApplicationName();
        manifest.setBaseURL(server.getStreamUrl() + "/" + appName + "/_definst_");

        manifest.setStreamType((isVodResource) ? "recorded" : "live");


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

        
        List<Media> medias = new ArrayList<Media>();
        Node resourceNode = session.getRootNode().getNode(resource.getPath().substring(1));
        manifest.setId(resourceNode.getName());

        NodeIterator itResourceNodes = resourceNode.getNodes();
        while (itResourceNodes.hasNext()) {
            Node mediaPathNode = itResourceNodes.nextNode();
            Media media = new Media();
            
            // Get media path
            String nodeMediaPath = mediaPathNode.getProperty("mediaPath").getValue().getString();
            String mediaHttpUrl = httpUrl.replace("http://", "http/");
            // Media path should be with prefix for VOD and only the streamname for LIVE
            String mediaPath = (isVodResource) ? "mp4:" + mediaHttpUrl + nodeMediaPath : nodeMediaPath;
            media.setMediaURL(mediaPath);
            
            // Get bitrate
            if (mediaPathNode.hasProperty("audioBitrate")) {
                Long audioBitrate = mediaPathNode.getProperty("audioBitrate").getValue().getLong();
                media.setAudioBitrate(audioBitrate);
            }

            // Get bitrate
            if (mediaPathNode.hasProperty("videoBitrate")) {
                Long videoBitrate = mediaPathNode.getProperty("videoBitrate").getValue().getLong();
                media.setVideoBitrate(videoBitrate);
            }

            // Get bitrate
            if (mediaPathNode.hasProperty("bitrate")) {
                Long bitrate = mediaPathNode.getProperty("bitrate").getValue().getLong();
                media.setVideoBitrate(bitrate);
                media.setAudioBitrate(0L);
            }            
            
            // Get widht
            if (mediaPathNode.hasProperty("width")) {
                Long width = mediaPathNode.getProperty("width").getValue().getLong();
                media.setWidth(width);
            }

            // Get height
            if (mediaPathNode.hasProperty("height")) {
                Long height = mediaPathNode.getProperty("height").getValue().getLong();
                media.setHeight(height);
            }

            // Get video codec
            if (mediaPathNode.hasProperty("videoCodec")) {
                String videoCodec = mediaPathNode.getProperty("videoCodec").getValue().getString();
                media.setVideoCodec(videoCodec);
            }

            // Get audio codec
            if (mediaPathNode.hasProperty("audioCodec")) {
                String audioCodec = mediaPathNode.getProperty("audioCodec").getValue().getString();
                media.setAudioCodec(audioCodec);
            }


            medias.add(media);            

        }
        
        manifest.setMedias(medias);
        return manifest;
    }

}


