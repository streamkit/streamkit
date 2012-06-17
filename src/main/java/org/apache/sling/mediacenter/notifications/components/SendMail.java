package org.apache.sling.mediacenter.notifications.components;

/**
 * @author Cosmin Stanciu
 */
public interface SendMail {
    public boolean send(String title, String content, String emailTo);
}
