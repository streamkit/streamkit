package org.apache.sling.service.postprocessing;

import static org.junit.Assert.assertEquals;


import org.apache.sling.api.SlingConstants;
import org.apache.sling.jackrabbit.AbstractJackrabbitTestCase;
import org.apache.sling.service.postprocessing.dto.MediaProperties;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

import javax.jcr.Binary;
import javax.jcr.Node;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;


/**
 * @author Cosmin Stanciu
 */
public class EventHandlerServiceTest extends AbstractJackrabbitTestCase {

    private final String TEST_FILE_NAME_OK = "sample_ok.mp4";
    private final String TEST_FILE_NAME_FAULTY = "sample_faulty.mp4";
    private String TEST_FILE_PATH_OK = null;
    private String TEST_FILE_PATH_FAULTY = null;

    private EventHandlerMediaAdded ehs = new EventHandlerMediaAdded();
    private String propPath =  "/content/channel/adventist_at/ondemand/27_1311867496192/mediaFile";

    // @Before
    public void init()  {
        // Find path to package on disk
        Class myClass = FFmpegService.class;
        URL url = myClass.getResource("FFmpegServiceTest.class");

        String path = url.getPath();
        String pathToFile = path.substring(0, path.lastIndexOf("/") + 1);
        TEST_FILE_PATH_OK = pathToFile +  TEST_FILE_NAME_OK;
        TEST_FILE_PATH_FAULTY = pathToFile +  TEST_FILE_NAME_FAULTY;
    }


    // @Test
    public void getChannel_channelName() {
        assertEquals("adventist_at", ehs.getChannel(propPath));
    }

    // @Test
    public void getFilePathDir_dirPath() {
        assertEquals("/content/channel/adventist_at/ondemand", ehs.getVideoDirPath(propPath));
    }

    // @Test
    public void getFilePath_filePath() {
        // assertEquals("/content/channel/adventist_at/ondemand/27_1311867496192.mp4", ehs.getVideoPath(propPath));
    }

    // @Test
    public void persistMediaProperties_setProperties() throws Exception {
        startRepository();
        String nodePath =  "content/channel/adventist_at/content_1";
        String codec = "h264";
        int height = 230;
        int width = 450;

        Node content = session.getRootNode().addNode("content");
        Node channel = content.addNode("channel");
        Node adventist_at = channel.addNode("adventist_at");
        Node content_1 = adventist_at.addNode("content_1");
        Node mediaFile = content_1.addNode("mediaFile");
        MediaProperties props = new MediaProperties();
        props.setWidth(width);
        props.setHeight(height);


        ehs.persistMediaProperties(props, mediaFile);

        session.save();

        Node createdNode = session.getRootNode().getNode(nodePath);
        assertEquals("content_1", createdNode.getName());
        assertEquals(codec, createdNode.getProperty("jcr:codec").getValue().getString());
        assertEquals(height, createdNode.getProperty("jcr:height").getValue().getString());
    }



}
