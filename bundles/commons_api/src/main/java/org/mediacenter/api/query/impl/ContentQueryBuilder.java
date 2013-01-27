package org.mediacenter.api.query.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.mediacenter.resource.MediaCenterResourceType;

/** Utility class that builds the statement for a content query */
public class ContentQueryBuilder
{
    private String additionalFields = "@album,@active,@title,@jcr:created,@created,@jcr:createdBy";

    public String getXPathQueryForAlbum(Node albumNode) throws RepositoryException
    {
        return getXPathQueryForAlbum(albumNode, "");
    }

    public String getXPathQueryForAlbum(Node albumNode, String searchTerm) throws RepositoryException
    {
        String expression = "";
        String albumName = albumNode.getName();
        if (searchTerm == null)
        {
            searchTerm = "";
        }
        expression = "/jcr:root" + albumNode.getPath();
        expression += "//(@title)[jcr:contains(.,'" + searchTerm + "*'),jcr:contains(@album,'*" + albumName + "* OR " +
                albumName + "'),sling:resourceType='" + MediaCenterResourceType.VOD + "']";

        // additional fields may be specified by adding "&property=my-prop" to the request
        expression += "/(" + additionalFields + ")";

        //add order by clause
        if (searchTerm.equals(""))
        {
            expression += "order by @jcr:created descending";
        }
        else
        {
            expression += "order by @jcr:score descending, @jcr:created descending";
        }

        return expression;
    }

    public String getXPathQueryForChannel(Node channelNode) throws RepositoryException
    {
        return getXPathQueryForChannel(channelNode, "");
    }

    public String getXPathQueryForChannel(Node channelNode, String searchTerm) throws RepositoryException
    {
        String expression;
        if (searchTerm == null)
        {
            searchTerm = "";
        }
        expression = "/jcr:root" + channelNode.getPath();
        expression += "//(@title)[jcr:contains(.,'" + searchTerm + "*')" +
                ",sling:resourceType='" + MediaCenterResourceType.VOD + "']";
        // additional fields may be specified by adding "&property=my-prop" to the request
        expression += "/(" + additionalFields + ")";

        //add order by clause
        if (searchTerm.equals(""))
        {
            expression += "order by @jcr:created descending";
        }
        else
        {
            expression += "order by @jcr:score descending, @jcr:created descending";
        }

        return expression;
    }


}
