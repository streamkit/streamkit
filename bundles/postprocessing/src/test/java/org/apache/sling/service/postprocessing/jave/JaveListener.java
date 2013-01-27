package org.apache.sling.service.postprocessing.jave;

/**
 * @author Cosmin Stanciu
 */
public class JaveListener implements EncoderProgressListener {
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
