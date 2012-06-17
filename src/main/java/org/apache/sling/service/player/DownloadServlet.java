package org.apache.sling.service.player;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Cosmin Stanciu
 */
@Component(immediate=true)
@Service
@Properties({
        @Property(name="service.description", value="Mediacenter - VOD download"),
        @Property(name="sling.servlet.resourceTypes", value= "mediacenter:vod"),
        @Property(name="sling.servlet.selectors", value= "download"),
        @Property(name="sling.servlet.extensions", value= "file")
})
public class DownloadServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        Resource resource = request.getResource();
        if (ResourceUtil.isNonExistingResource(resource)) {
            throw new ResourceNotFoundException("No data to render.");
        }

        // String resPath = resource.getPath();
        // String absoluteFilelPath = "/Users/selfxp/Downloads/Studiul_5_20110723151827363.mp4";
        String absoluteFilelPath = "";
        File file = new File(absoluteFilelPath);
        
        response.setContentType("video/mp4");
        response.setHeader("Content-Disposition","attachment; filename=\"" + file.getName() + "\";");
        response.setContentLength((int)file.length());

        ServletOutputStream out = response.getOutputStream();
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(absoluteFilelPath);
            byte[] buffer = new byte[1024000];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

        }
        finally {
            if(inputStream != null) {
                inputStream.close(); 
            }
            out.close();
        }


    }
}
