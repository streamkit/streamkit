package org.apache.sling.service.postprocessing;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.JobProcessor;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


@Component(immediate=true)
@Properties({
   @org.apache.felix.scr.annotations.Property(name="service.description", value="Mediacenter - EventHandler on upload file"),
   @org.apache.felix.scr.annotations.Property(name="event.topics", value=org.apache.sling.api.SlingConstants.TOPIC_RESOURCE_ADDED)
})
@Service
public class EventHandlerMediaAdded implements JobProcessor, EventHandler {
    private Session session = null;
    private static final String JCR_MEDIA_PATH_CONFIG = "config/storage/servers";
    private static String MEDIA_ABSOLUTE_PATH = null;
    private static String MEDIA_HDD_PATH = null;
    private Exception e;

    @Reference
    private LogService logger;

    @Reference
    private SlingRepository repository;

    @Reference
	private EventAdmin eventAdmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private FFmpegService ffmpegService;

    protected void activate(ComponentContext context)  throws Exception {
        session = repository.loginAdministrative(null);
        getStoragePaths();
    }
    protected void deactivate(ComponentContext componentContext) throws RepositoryException {
        if (session != null) {
            session.logout();
            session = null;
        }
    }

	public void handleEvent(Event event) {
        process(event);
	}

    private void getStoragePaths() throws Exception {
        Node storageServersNode = session.getRootNode().getNode(JCR_MEDIA_PATH_CONFIG);
        MEDIA_ABSOLUTE_PATH = storageServersNode.getProperty("rootPath").getValue().getString();
        String activeServer = storageServersNode.getProperty("activeServer").getValue().getString();
        MEDIA_HDD_PATH = session.getRootNode().getNode(JCR_MEDIA_PATH_CONFIG + "/" + activeServer).getProperty("path").getValue().getString();
    }

    // When a new media file is added to JCR, copy the file to disk and apply ffmpeg to read all properties
    public boolean process(Event event) {

        String propPath = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
        String propResType = (String) event.getProperty(SlingConstants.PROPERTY_RESOURCE_TYPE);
        
        logger.log(LogService.LOG_INFO, "Event resource added: path -> " + propPath + ", resType -> " + propResType);

         if (propPath.endsWith("mediaFile") && ("nt:file".equals(propResType) || "nt:resource".equals(propResType))) {

            logger.log(LogService.LOG_INFO, "Starting thread for resourceType:  " + propResType);
            FFmpegProcessorThread pt = new FFmpegProcessorThread(repository,  MEDIA_ABSOLUTE_PATH, MEDIA_HDD_PATH, propPath, ffmpegService, logger, eventAdmin);
            new Thread(pt).start();
         }

        return true;
    }
}
