package org.apache.sling.service.postprocessing;

import org.apache.sling.service.postprocessing.jave.FFMPEGLocator;

/**
 * @author Cosmin Stanciu
 */
public class FFmpegExecutableLocatorSling extends FFMPEGLocator {

    private String path;
    public FFmpegExecutableLocatorSling(String ffmpegDiskPath) {
        this.path = ffmpegDiskPath;
    }

    @Override
    protected String getFFMPEGExecutablePath() {
        return path;
    }
}
