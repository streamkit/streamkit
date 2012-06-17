package org.apache.sling.cdn.service.interfaces;

import java.util.List;

/**
 * @author Cosmin Stanciu
 */
public interface CDNService {
    public List<CDNServer> hostAddresses(String ipAddress);
    public Long connectionCounts(String streamName);
}
