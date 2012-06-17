package org.apache.sling.mediacenter.notifications.components;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.api.SlingRepository;

/**
 * @author Cosmin Stanciu
 */
@Component
public class PersistNotificationComponent {

    @Reference
    private SlingRepository repository;

    public void persist() {
        System.out.println("persisted to jcr");
    }
}
