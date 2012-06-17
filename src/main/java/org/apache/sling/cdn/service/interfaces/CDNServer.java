package org.apache.sling.cdn.service.interfaces;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Stanciu
 */
public class CDNServer {
    
    private String streamUrl;
    private String vodApplicationName;
    private String liveApplicationName;
    private Map<String, Long> streamConnectionCount = new HashMap<String, Long>();


    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getVodApplicationName() {
        return vodApplicationName;
    }

    public void setVodApplicationName(String vodApplicationName) {
        this.vodApplicationName = vodApplicationName;
    }

    public String getLiveApplicationName() {
        return liveApplicationName;
    }

    public void setLiveApplicationName(String liveApplicationName) {
        this.liveApplicationName = liveApplicationName;
    }

    public Map<String, Long> getStreamConnectionCount() {
        return streamConnectionCount;
    }

    public void setStreamConnectionCount(Map<String, Long> streamConnectionCount) {
        this.streamConnectionCount = streamConnectionCount;
    }
}
