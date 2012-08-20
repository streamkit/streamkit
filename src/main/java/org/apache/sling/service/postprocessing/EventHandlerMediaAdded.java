package org.apache.sling.service.postprocessing;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.event.JobProcessor;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrResourceResolverFactory;
import org.apache.sling.service.postprocessing.dto.MediaProperties;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.*;


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
    private String channel = null;
    private Exception e;

    @Reference
    private LogService logger;

    @Reference
    private SlingRepository repository;

    @Reference
    private JcrResourceResolverFactory resolverFactory;

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

         if (!propPath.endsWith("mediaFile") && propResType.equals("mediacenter:vod")) {

             String videoDirPath = getVideoDirPath(propPath);
             String absoluteVideoDirPath = MEDIA_ABSOLUTE_PATH + "/" + MEDIA_HDD_PATH + videoDirPath;
             String videoPath = getVideoPath(propPath, MEDIA_HDD_PATH);
             String absoluteVideoPath = MEDIA_ABSOLUTE_PATH + "/" + videoPath;
             String snapshotPath = getSnapshotPath(videoPath);
             String absoluteSnapshotPath = MEDIA_ABSOLUTE_PATH + "/" + snapshotPath;

            channel = getChannel(propPath);
            try {
                session = (session != null) ? session : repository.loginAdministrative(null);
                String jcrContentNodePathShort = propPath.substring(1) + "/mediaFile";
                String jcrContentNodePathLong = jcrContentNodePathShort + "/jcr:content";
                // Try both {..}/mediaFile and {..}/mediaFile/jcr:content  
                String jcrContentNodePath = (session.getRootNode().hasNode(jcrContentNodePathLong)) ? jcrContentNodePathLong : jcrContentNodePathShort;
                Node dataNode = session.getRootNode().getNode(jcrContentNodePath);
                Property mimeTypeProperty = dataNode.getProperty("jcr:mimeType");
                String mimeType = mimeTypeProperty.getValue().getString();
                if ("video/mp4".equals(mimeType) || "video/mpeg4".equals(mimeType) || "video/x-flv".equals(mimeType)) {

                    InputStream fileInputStrem = dataNode.getProperty("jcr:data").getBinary().getStream();

                    // 1. Copy the file stored to repository to a physical location on disc
                    outputFile(fileInputStrem, absoluteVideoDirPath, absoluteVideoPath);

                    // 2. Read all video properties using FFmpeg
                    MediaProperties props = ffmpegService.extractFFmpegFileInfo(absoluteVideoPath);
                    props.setVideoPath(videoPath);

                    // 3. Create media file screenshot
                    ffmpegService.generateSnapshot(absoluteVideoPath, props.getDuration(), props.getWidth(), props.getHeight());
                    props.setSnapshotPath(snapshotPath);

                    logger.log(LogService.LOG_INFO, "SnapshotPath set: " + snapshotPath);


                    // 4. Persist all properties to a JCR node
                    persistMediaProperties(props, dataNode);

                    // 5. Remove mediaFile node
                    dataNode.remove();
                    logger.log(LogService.LOG_INFO, "FieleNode removed: " + dataNode.getIdentifier());

                    session.save();

                }
            } catch (Exception e) {
                this.e = e;
                logger.log(LogService.LOG_ERROR, "process exception: " + e.getMessage(), e);
                removeFileOnError(absoluteVideoPath, absoluteSnapshotPath);


            } finally {
                // Send notification informing channel owner about
                Notification notif = new Notification(eventAdmin, session, e, propPath);
                notif.sendNotification();

                if (session != null) {
                    session.logout();
                    session = null;
                }

            }
        }
        return true;
    }

    
    // Remove ffmpeg files when error occurred
    private void removeFileOnError(String absoluteVideoPath, String absoluteSnapshotPath) {
        // If exception occurred, remove file from disk
        File mediaFile = new File(absoluteVideoPath);
        if (mediaFile.exists()){
            mediaFile.delete();
        }

        // If exception occurred, remove snapshot from disk
        File imageFile = new File(absoluteSnapshotPath);
        if (imageFile.exists()){
            imageFile.delete();
        }
    }

    // Extract the channel name from propPath event
    protected String getChannel(String propPath) {
            propPath = propPath.substring(propPath.indexOf("channel") + 8, propPath.length());
            propPath = propPath.substring(0, propPath.indexOf("/"));
        return propPath;
    }

    // Persist all video properties to /content/channel/{channelName}/ondemand/{contentName}/properties
    protected void persistMediaProperties(MediaProperties props, Node fileNode) throws Exception {
        Node mediaNode = fileNode.getParent();
        mediaNode.setProperty("snapshotPath", props.getSnapshotPath());
        mediaNode.setProperty("duration", props.getDuration());
        
        Node streamNode = mediaNode.addNode(props.getCodec() + "_" + props.getBitrate());
        streamNode.setProperty("mediaPath", props.getVideoPath());
        streamNode.setProperty("codec", props.getCodec());
        streamNode.setProperty("fps", props.getFps());
        streamNode.setProperty("bitrate", props.getBitrate());
        streamNode.setProperty("width", props.getWidth());
        streamNode.setProperty("height", props.getHeight());
    }

    // Create the file on hard disk from the inputStream received from JCR
    private void outputFile (InputStream inputStream, String absoluteDirPath, String absoluteFilePath) throws IOException {
        //create the directory where the file will be saved
        File dir = new File(absoluteDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // create the media file
        File f = new File(absoluteFilePath);

        OutputStream out=new FileOutputStream(f);
        byte buf[]=new byte[2048];
        int len;
        while((len=inputStream.read(buf)) > 0)
        out.write(buf, 0, len);
        out.close();
        inputStream.close();
    }

    protected String getVideoDirPath(String propPath) {
        String path = propPath.replace("/mediaFile", "");
        path = path.substring(0, path.lastIndexOf("/"));
        return path;
    }

    protected String getVideoPath(String propPath, String mediaHddPath) {
        String dirPath = propPath.substring(0, propPath.lastIndexOf("/"));
        String fileName = propPath.substring(propPath.lastIndexOf("/") + 1, propPath.length());
        fileName = fileName.replaceAll("\\W", "_");
        return mediaHddPath + dirPath + "/" + fileName +  ".mp4";
    }

    protected String getSnapshotPath(String mediaPath) {
        return mediaPath.replace("mp4", "jpg");
    }
}
