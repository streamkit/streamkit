package org.apache.sling.service.rss;

/**
 * @author Cosmin Stanciu
 */
import org.apache.sling.api.SlingHttpServletResponse;

import java.io.PrintWriter;

public class RSSFeedWriter {

    private Feed rssfeed;
    private SlingHttpServletResponse response;
    private String rssURL;

    public RSSFeedWriter(Feed rssfeed, SlingHttpServletResponse response, String rssURL) {
        this.rssfeed = rssfeed;
        this.response = response;
        this.rssURL = rssURL;
    }

    public void write() {

        PrintWriter out = null;

        try {
            out = response.getWriter();
            out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
            out.println("<rss version=\"2.0\">");
            // out.println("<atom:link href=\"" + rssURL + "\" rel=\"self\" type=\"application/rss+xml\" />");


            out.println("<channel>");

            createNode(out, "title", rssfeed.getTitle());
            createNode(out, "link", rssfeed.getLink());
            createNode(out, "description", rssfeed.getDescription());
            createNode(out, "language", rssfeed.getLanguage());
            createNode(out, "copyright", rssfeed.getCopyright());
            createNode(out, "pubDate", rssfeed.getPubDate());


            for (FeedMessage entry : rssfeed.getMessages()) {
                out.println("<item>");

                    createNode(out, "id", entry.getGuid());
                    createNode(out, "title", entry.getTitle());
                    createNode(out, "description", entry.getDescription());
                    createNode(out, "link", entry.getLink());
                    createNode(out, "pubDate", entry.getPubDate());
                    out.println("<guid isPermaLink=\"true\">" + entry.getLink() + "</guid>");
                    out.println("<media:player url=\"" + entry.getLink() + "\" height=\"580\" width=\"640\" />");
                    out.println("<media:thumbnail url=\"" + entry.getImage() + "\" />");
                    out.println("<media:credit role=\"author\">" + entry.getAuthor() + "\"</media:credit>");
                    createNode(out, "media:keywords", entry.getTags());
                    createNode(out, "media:description", entry.getDescription());

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
