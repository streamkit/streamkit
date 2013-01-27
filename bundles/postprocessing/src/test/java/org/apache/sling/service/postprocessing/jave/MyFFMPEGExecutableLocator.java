package org.apache.sling.service.postprocessing.jave;

/**
 * @author Cosmin Stanciu
 */
public class MyFFMPEGExecutableLocator extends FFMPEGLocator {


    @Override
    protected String getFFMPEGExecutablePath() {
        return "ffmpeg";
    }
}
