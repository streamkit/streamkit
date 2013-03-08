package org.streamkit.vod.post.processor;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.service.postprocessing.FFmpegService;
import org.apache.sling.service.postprocessing.Notification;
import org.apache.sling.service.postprocessing.dto.MediaProperties;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.streamkit.vod.post.VodPostProcessor;
import org.streamkit.vod.post.VodPostProcessorBaseImpl;

import javax.jcr.*;
import java.io.*;


@Component(immediate = true, metatype = false)
@Properties({
   @Property(name = "order", intValue = 10)
})
@Service(value = { VodPostProcessor.class })
public class FFmpegProcessor extends VodPostProcessorBaseImpl implements VodPostProcessor {
    private Session session = null;
    private static final String JCR_MEDIA_PATH_CONFIG = "config/storage/servers";
    private static String MEDIA_ABSOLUTE_PATH = null;
    private static String MEDIA_HDD_PATH = null;
    private String propPath;
    private boolean sendNotif = true;
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

    private void getStoragePaths() throws Exception {
        Node storageServersNode = session.getRootNode().getNode(JCR_MEDIA_PATH_CONFIG);
        MEDIA_ABSOLUTE_PATH = storageServersNode.getProperty("rootPath").getValue().getString();
        String activeServer = storageServersNode.getProperty("activeServer").getValue().getString();
        MEDIA_HDD_PATH = session.getRootNode().getNode(JCR_MEDIA_PATH_CONFIG + "/" + activeServer).getProperty("path").getValue().getString();
    }




    public Boolean processAdded (Node videoNode) throws RepositoryException {
        // String propPath = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
        // String propResType = (String) event.getProperty(SlingConstants.PROPERTY_RESOURCE_TYPE);

        if ( !ensureMediaFileExists(videoNode) ) {
            return false;
        }

        propPath = videoNode.getPath();

        runFFmpeg();
//        FFmpegProcessorThread pt = new FFmpegProcessorThread(repository,  MEDIA_ABSOLUTE_PATH, MEDIA_HDD_PATH, propPath, ffmpegService, logger, eventAdmin);
//        new Thread(pt).start();
//        logger.log(LogService.LOG_INFO, "Event resource added: path -> " + propPath + ", resType -> " + propResType);
//        if (propPath.endsWith("mediaFile") && ("nt:file".equals(propResType) || "nt:resource".equals(propResType))) {
//            logger.log(LogService.LOG_INFO, "Starting thread for resourceType:  " + propResType);
//        }

        return true;
    }



    public void runFFmpeg() {
        String videoDirPath = getVideoDirPath(propPath);
        String absoluteVideoDirPath = MEDIA_ABSOLUTE_PATH + "/" + MEDIA_HDD_PATH + videoDirPath;
        String videoPath = getVideoPath(propPath, videoDirPath, MEDIA_HDD_PATH);
        String absoluteVideoPath = MEDIA_ABSOLUTE_PATH + "/" + videoPath;
        String snapshotPath = getSnapshotPath(videoPath);
        String absoluteSnapshotPath = MEDIA_ABSOLUTE_PATH + "/" + snapshotPath;

        // String channel = getChannel(propPath);
        Session session = null;
        try {

            String jcrContentNodePathShort = propPath.substring(1);
            String jcrContentNodePathLong = jcrContentNodePathShort + "/jcr:content";
            // Try both {..}/mediaFile and {..}/mediaFile/jcr:content
            session = repository.loginAdministrative(null);
            String jcrContentNodePath = (session.getRootNode().hasNode(jcrContentNodePathLong)) ? jcrContentNodePathLong : jcrContentNodePathShort;

            Node dataNode = session.getRootNode().getNode(jcrContentNodePath);

            javax.jcr.Property mimeTypeProperty = dataNode.getProperty("jcr:mimeType");
            String mimeType = mimeTypeProperty.getValue().getString();
            if ("video/mp4".equals(mimeType) ||
                    "video/mpeg4".equals(mimeType) ||
                    "video/x-flv".equals(mimeType) ||
                    "video/x-f4v".equals(mimeType)) {

                logger.log(LogService.LOG_INFO, "Reading file jcr");

                // 1. Copy the file stored to repository to a physical location on disc
                outputFile(dataNode, absoluteVideoDirPath, absoluteVideoPath);
                logger.log(LogService.LOG_INFO, "File copied to path: " + absoluteVideoPath);

                // 2. Read all video properties using FFmpeg
                MediaProperties props = ffmpegService.extractFFmpegFileInfo(absoluteVideoPath);
                props.setVideoPath(videoPath);

                // 3. Create media file screenshot
                ffmpegService.generateSnapshot(absoluteVideoPath, props.getDuration(), props.getWidth(), props.getHeight());
                props.setSnapshotPath(snapshotPath);

                logger.log(LogService.LOG_INFO, "SnapshotPath set: " + snapshotPath);

                if ( "nt:file".equals(dataNode.getParent().getPrimaryNodeType().getName()) ) {
                    dataNode = dataNode.getParent();
                }

                // 4. Persist all properties to a JCR node
                persistMediaProperties(props, dataNode.getParent());

                // 5. Remove mediaFile node
                dataNode.remove();
                logger.log(LogService.LOG_INFO, "Fiele Node removed: " + dataNode.getIdentifier());

                session.save();

                // in case video mime type not supported
            } else {
                throw new Exception("This video format is not supported: " + mimeType);
            }
        } catch (ItemNotFoundException infe) {
            logger.log(LogService.LOG_DEBUG, "Node has been already removed!", infe.fillInStackTrace());
            sendNotif = false;

        } catch (InvalidItemStateException iise) {
            logger.log(LogService.LOG_DEBUG, "Node has been already removed!", iise.fillInStackTrace());
            sendNotif = false;

        } catch (Exception e) {
            this.e = e;
            logger.log(LogService.LOG_ERROR, "process exception: " + e.getMessage(), e);
            removeFileOnError(absoluteVideoPath, absoluteSnapshotPath);

        } finally {
            if (session != null) {
                session.logout();
                session = null;
            }
        }

        if (sendNotif) {
            // Send notification informing channel owner about the postprocessing status
            Notification notif = new Notification(eventAdmin, repository, e, propPath);
            notif.send();
        }
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
        propPath = propPath.replace("/mediaFile", "");
        return propPath;
    }

    // Persist all video properties to /content/channel/{channelName}/ondemand/{contentName}/properties
    protected void persistMediaProperties(MediaProperties props, Node fileNode) throws Exception {
        Node mediaNode = fileNode;

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
    private void outputFile (Node dataNode, String absoluteDirPath, String absoluteFilePath) throws IOException {
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
        InputStream fileInputStream = null;
        try {
            fileInputStream = dataNode.getProperty("jcr:data").getBinary().getStream();
            while((len=fileInputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (Exception ex) {
            logger.log(LogService.LOG_ERROR, "Error on saving file: " + ex.getLocalizedMessage());
        } finally {
            out.close();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    protected String getVideoDirPath(String propPath) {
        String path = propPath.replace("/mediaFile", "");
        path = path.substring(0, path.lastIndexOf("/"));
        return path;
    }

    protected String getVideoPath(String propPath, String dirPath, String mediaHddPath) {
        String propPathCrop = propPath.replace("/mediaFile", "");
        String fileName = propPathCrop.substring(propPathCrop.lastIndexOf("/") + 1, propPathCrop.length());
        fileName = fileName.replaceAll("\\W", "_");
        return mediaHddPath + dirPath + "/" + fileName +  ".mp4";
    }

    protected String getSnapshotPath(String mediaPath) {
        return mediaPath.replace("mp4", "jpg");
    }

    private Boolean ensureMediaFileExists( Node videoNode) {

        try {
            videoNode.getNode("mediaFile");
            return true;
        } catch (RepositoryException e1) {
            return false;
        }
    }

    public Boolean processUpdated(Node videoNode) {
        return null;
    }

    public int getExecutionOrder() {
        return 0;
    }
}
