package org.apache.sling.service.postprocessing;

import org.apache.sling.service.postprocessing.jave.EncoderProgressListener;
import org.apache.sling.service.postprocessing.jave.MultimediaInfo;

/**
 * @author Cosmin Stanciu
 */
public class FFmpegTranscodingListernet implements EncoderProgressListener {
    public void sourceInfo(MultimediaInfo info) {
        System.out.println("MultimediaInfoFormat: " + info.getFormat());
        System.out.println("MultimediaInfoDuration: " + info.getDuration());
    }

    public void progress(int permil) {
       System.out.println("Progress: " + permil);
    }

    public void message(String message) {
        System.out.println("LIstener message: " + message);
    }
}
