package org.apache.sling.service.postprocessing;


import java.io.File;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.service.postprocessing.dto.MediaProperties;
import org.apache.sling.service.postprocessing.exception.PostprocessingException;
import org.apache.sling.service.postprocessing.jave.*;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(immediate = true, label="FFmpegService", description="FFmpegService description")
@Properties({
    @Property(name="service.description",value="Factory for configuration based request/access loggers"),
    @Property(name="service.vendor",value="The Apache Software Foundation")
})
@Service (value = FFmpegService.class)
public class FFmpegService {
    private static MediaProperties prop = new MediaProperties();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private SlingRepository repository;

	protected final static Logger log = LoggerFactory.getLogger(FFmpegService.class);
    private static final String JCR_CDN_SERVERS_PATH = "config/postprocessing/ffmpeg";
    private static String FFMPEG_PATH = null;
	private static final String ACCEPTED_CODEC = "h264";
    private Session session = null;


    private Encoder encoder = null;
    // private FFmpegTranscodingListernet listener = new FFmpegTranscodingListernet();

    protected void activate(ComponentContext context)  throws Exception {
        session = repository.loginAdministrative(null);
        FFMPEG_PATH = getFFmpegPath();
        encoder = new Encoder(new FFmpegExecutableLocatorSling(FFMPEG_PATH));
    }
    protected void deactivate(ComponentContext componentContext) throws RepositoryException {
        if (session != null) {
            session.logout();
            session = null;
        }
    }

    private String getFFmpegPath() throws Exception {
        Node storageNode = session.getRootNode().getNode(JCR_CDN_SERVERS_PATH);
        return storageNode.getProperty("path").getValue().getString();
    }

	// Get the info of the media file
	public MediaProperties extractFFmpegFileInfo(String mediaFilePath) throws PostprocessingException {

        File mediaFile = new File(mediaFilePath);
        if (!mediaFile.exists()) {
            log.error("The file: " + mediaFilePath + ", doesn't exist on server!");
        }

        MultimediaInfo mInfo = new MultimediaInfo();
        try {
            mInfo = encoder.getInfoShort(mediaFile);
        } catch (Exception e) {
            throw new PostprocessingException(PostprocessingException.COMPILING, e);
        }

        // Duration
        long duration = mInfo.getDuration();
        prop.setDuration(duration);
        log.debug("Duration: " + duration);

        // Kb/s
        int kbs = mInfo.getVideo().getBitRate();
        // propertiesNode.setProperty("jcr:kbs", kbs);
        prop.setBitrate(kbs);
        log.info("Kbs: " + kbs);

        // Codec
        String codec = mInfo.getVideo().getDecoder();
        if (!acceptedCodec(codec)) {
            throw new PostprocessingException(PostprocessingException.CODEC,
                    "Used codec is: " + codec + ", but the application only supports h264/mpeg4 encoded videos. " +
                    "Please re-encode your file.");
        }
        prop.setCodec(codec);
        log.info("Codec: " + codec);

        // Resolution
        int width = mInfo.getVideo().getSize().getWidth();
        int height = mInfo.getVideo().getSize().getHeight();
        prop.setHeight(height);
        prop.setWidth(width);

        // Fps
        float fps = mInfo.getVideo().getFrameRate();
        prop.setFps(fps);
        log.info("Fps: " + fps);

        return prop;
	}


    // Codec accepted by the system
    //TODO: These codecs should added to project configuration
    private boolean acceptedCodec (String codec) {
        if ("h264".equals(codec)) {
            return true;
        }

        if ("mpeg4".equals(codec)) {
            return true;
        }
        return false;
    }



	public void generateSnapshot(String mediaFilePath, long duration, int width, int height) throws PostprocessingException {
        try {
            File mediaFile = new File(mediaFilePath);
            String snapshotDirPath = mediaFilePath.substring(0, mediaFilePath.lastIndexOf("/"));
            File snapshotDir = new File(snapshotDirPath);
            if (!snapshotDir.exists()) {
                snapshotDir.mkdirs();
            }
            String snapshotFilePath = mediaFilePath.replace(mediaFilePath.substring(mediaFilePath.lastIndexOf("."), mediaFilePath.length()), ".jpg");
            File snapshotFile = new File(snapshotFilePath);
            EncodingAttributes attrs = new EncodingAttributes();
            VideoAttributes video = new VideoAttributes();
            video.setSize(new VideoSize(width, height));
            attrs.setFormat("mjpeg");
            attrs.setDuration(1f);
            // Generate screenshot in the middle of the movie
            float offset = duration / 1000 / 2;
            attrs.setOffset(offset);
            attrs.setVideoAttributes(video);
            encoder.encode(mediaFile, snapshotFile, attrs);

        } catch (EncoderException e) {
            throw new PostprocessingException(PostprocessingException.GENERAL,
                    "An error occurred when trying to generate screenshot:  " + e.getLocalizedMessage());
        }
	}
}
