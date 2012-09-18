package org.apache.sling.service.rss;


import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(immediate=true)
@Service
@Properties({
        @Property(name = "service.description", value="MediaCenter RSS Servlet"),
        @Property(name = "service.vendor", value="org.mediaCenter"),
        @Property(name = "sling.servlet.resourceTypes", value={"mediacenter:channel"}),
        @Property(name = "sling.servlet.selectors", value="library"),
        @Property(name = "sling.servlet.extensions", value="rss"),
        @Property(name = "sling.servlet.prefix", value="-1", propertyPrivate = true)
})
public class RSSServlet extends SlingSafeMethodsServlet  {

    private static String HTTP_SERVER_URL;

    /** Query type */
    public static final String QUERY_TYPE = "queryType";

    /** Search clause */
    public static final String STATEMENT = "statement";

    @Reference
    private SlingRepository repository;

    private Session session;

    SimpleDateFormat sdf = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

    @Activate
    protected void activateactivate(BundleContext bundleContext, Map<String, Object> props)   throws Exception {
        session = repository.loginAdministrative(null);

        Node storageNode = session.getRootNode().getNode( "config/http/server");
        HTTP_SERVER_URL = storageNode.getProperty("httpUrl").getValue().getString();
    }
    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException {
        if (session != null) {
            session.logout();
            session = null;
        }
    }


    @Override
    protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp) throws ServletException, IOException {
        try {
        ResourceResolver resolver = req.getResourceResolver();
        Resource resource = req.getResource();

        String queryType = getQueryType(req);

        String statement = getStatement();

        statement = checkJcrRoot(req, statement);

        Iterator<Map<String, Object>> result = resolver.queryResources(statement, queryType);


        resp.setContentType("text/rss+xml;charset=ISO-8859-1");
        resp.setContentType("text/rss+xml");


        Node channelNode = session.getRootNode().getNode(resource.getPath().substring(1));
        String channelName = channelNode.getProperty("title").getValue().getString();
        String rssURL = req.getRequestURL().toString();

        Feed rssFeeder = new Feed(channelName, rssURL, "", "en", channelName, sdf.format(new Date()));

        while(result.hasNext()) {
            FeedMessage feed = new FeedMessage();
            Map<String, Object> row = result.next();

            String path = row.get("jcr:path").toString();
            
            // Retrieve snapshotPath value from jcr
            Node contentNode = session.getRootNode().getNode(path.substring(1));

            // Title
            feed.setTitle(contentNode.getProperty("title").getValue().getString());

            // ID
            feed.setGuid(contentNode.getIdentifier());

            // Snapshot path
            String snapshotPath = null;
            boolean snapshotPropertyExists = contentNode.hasProperty("snapshotPath");
            if (snapshotPropertyExists) {
                String serverURL = (HTTP_SERVER_URL.startsWith("http://")) ? HTTP_SERVER_URL : "http://" + HTTP_SERVER_URL;
                snapshotPath = serverURL + "/" + contentNode.getProperty("snapshotPath").getValue().getString();
            }
            feed.setImage(snapshotPath);

            // Player-url
            URI uri = new URI(
                    req.getScheme(),
                    req.getServerName(),
                    path + ".player.html/menu",
                    null);
            String playerPath = uri.toASCIIString();
            feed.setLink(playerPath);

            // Author
            feed.setAuthor(contentNode.getProperty("author").getValue().getString());

            //Description
            feed.setDescription(contentNode.getProperty("description").getValue().getString());

            // Tags
            feed.setTags(contentNode.getProperty("tags").getValue().getString());

            // Created
            Calendar date = contentNode.getProperty("jcr:created").getDate();
            feed.setPubDate(sdf.format(date.getTime()));

            rssFeeder.getMessages().add(feed);
        }

        // Now write the file
        RSSFeedWriter writer = new RSSFeedWriter(rssFeeder, resp, rssURL);
        writer.write();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private String checkJcrRoot(SlingHttpServletRequest req, String statement)
    {
        if ( statement.indexOf("jcr:root") < 0 )
        {
            statement = "/jcr:root" + req.getResource().getPath() + statement;
        }
        return statement;
    }

    /**
     * Retrieve the query type from the request.
     *
     * @param req request
     * @return the query type.
     *
     */
    protected String getQueryType(SlingHttpServletRequest req) {
        return "xpath";
    }

    /**
     * Retrieve the query statement from the request.
     *
     * @return the query statement.
     *
     */
    protected String getStatement() {
        return "//(@title)[sling:resourceType='mediacenter:vod']order by @jcr:created descending";
        // &property=title&property=created&property=jcr:created&property=active&rows=15&offset=45
    }

}

