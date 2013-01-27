package org.apache.sling.service.postprocessing;

import org.apache.sling.junit.annotations.SlingAnnotationsTestRunner;
import org.apache.sling.junit.annotations.TestReference;
import org.apache.sling.service.postprocessing.dto.MediaProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Stanciu
 */

@RunWith(SlingAnnotationsTestRunner.class)
public class FFmpegTranscoderServiceTest {

    @TestReference
    private FFmpegService ffmpeg;


    @Test
    public void testExtractFFmpegFileInfo() throws Exception {
        String mediaPath = "/Users/selfxp/Work/Temp/content/ondemand.mp4";
        MediaProperties mediaProps = ffmpeg.extractFFmpegFileInfo(mediaPath);
        mediaProps.getHeight();

        ffmpeg.generateSnapshot(mediaPath, mediaProps.getDuration(), mediaProps.getWidth(), mediaProps.getHeight());
    }

}
