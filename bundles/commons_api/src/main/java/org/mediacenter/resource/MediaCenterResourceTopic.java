package org.mediacenter.resource;

/**
 * Class listing available TOPICS for MediaCenter Resources
 */
public class MediaCenterResourceTopic
{
    /** The job topic used to process newly added content */
    public static final String VOD_ADDED_TOPIC = "org/mediacenter/eventing/vod/new/job";

    /** The job topic used for process existing content being edited */
    public static final String VOD_UPDATED_TOPIC = "org/mediacenter/eventing/vod/update/job";

}
