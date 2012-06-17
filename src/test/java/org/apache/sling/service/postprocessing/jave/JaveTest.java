package org.apache.sling.service.postprocessing.jave;

import org.junit.Test;

import java.io.File;

/**
 * @author Cosmin Stanciu
 */
public class JaveTest {

    @Test
    public void test() throws Exception {

        File source = new File("/Users/selfxp/Downloads/Sequence 01_1.mp4");
        File target = new File("/Users/selfxp/Downloads/test.jpeg");


//        VideoAttributes video = new VideoAttributes();
//        video.setSize(new VideoSize(400, 300));
//        EncodingAttributes attrs = new EncodingAttributes();
//        attrs.setOffset(5f);
//        attrs.setDuration(1f);
//
//
//        attrs.setFormat("mjpeg");
//        attrs.setVideoAttributes(video);
        Encoder encoder = new Encoder(new MyFFMPEGExecutableLocator());
//        JaveListener listener = new JaveListener();
//        encoder.encode(source, target, attrs, listener);
        MultimediaInfo mediaInfo = encoder.getInfoShort(source);
        System.out.println(mediaInfo);
    }

}
