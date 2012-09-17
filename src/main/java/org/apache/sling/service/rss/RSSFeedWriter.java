package org.apache.sling.service.rss;

/**
 * @author Cosmin Stanciu
 */
import org.apache.sling.api.SlingHttpServletResponse;

import java.io.PrintWriter;

public class RSSFeedWriter {

    private Feed rssfeed;
    private SlingHttpServletResponse response;

    public RSSFeedWriter(Feed rssfeed, SlingHttpServletResponse response) {
        this.rssfeed = rssfeed;
        this.response = response;
    }

    public void write() {

        PrintWriter out = null;

        try {
            out = response.getWriter();
            out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
            out.println("<rss version=\"2.0\">");

            out.println("<channel>");

            createNode(out, "title", rssfeed.getTitle());
            createNode(out, "link", rssfeed.getLink());
            createNode(out, "description", rssfeed.getDescription());
            createNode(out, "language", rssfeed.getLanguage());
            createNode(out, "copyright", rssfeed.getCopyright());
            createNode(out, "pubdate", rssfeed.getPubDate());


            for (FeedMessage entry : rssfeed.getMessages()) {
                out.println("<item>");

                    createNode(out, "title", entry.getTitle());
                    createNode(out, "description", entry.getDescription());
                    createNode(out, "link", entry.getLink());
                    createNode(out, "image", entry.getImage());
                    createNode(out, "author", entry.getAuthor());
                    createNode(out, "pubDate", entry.getPubDate());
                    createNode(out, "guid", entry.getGuid());

                out.println("</item>");
            }
            out.println("</channel>");
            out.println("</rss>");

        } catch (Exception e)  {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }

    private void createNode(PrintWriter out, String name, String value) {
        out.println("<"+name+">" + value + "</"+name+">");
    }
}
