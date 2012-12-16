/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.mediacenter.api.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.*;
import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.servlets.get.impl.helpers.JsonResourceWriter;
import org.mediacenter.resource.MediaCenterResourceType;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SlingSafeMethodsServlet that renders the search results as JSON data.
 * <p>
 * Currently this class is only a clone of JsonQueryServlet, and should be
 * optimized in the future.
 * </p>
 * <p/>
 * <p>
 * Sample usage: http://localhost:8080/content/channel/demo.content-query.tidy.json?queryType=xpath&statement=//*
 * or
 * http://localhost:8080/content/channel/demo.content-query.tidy.json?queryType=xpath&statement=//element(*,nt:unstructured)[@sling:resourceType='mediacenter:vod']/(@active,@title,@jcr:created,@jcr:createdBy)
 * <p/>
 * This servlet only serves content from the current node.
 * </p>
 * <p/>
 * Use this as the default query servlet for json get requests for MediaCenter Channels
 */
@Component(immediate = true, metatype = false,
        label = "ContentQueryServlet", description = "Default Query Servlet to search for videos")
@Service
@Properties({
        @Property(name = "service.description", value = "MediaCenter Query Servlet"),
        @Property(name = "service.vendor", value = "org.mediaCenter"),
        @Property(name = "sling.servlet.resourceTypes", value = {
                MediaCenterResourceType.CHANNEL,
                MediaCenterResourceType.LIBRARY,
                MediaCenterResourceType.ALBUM }),
        @Property(name = "sling.servlet.selectors", value = "content-query"),
        @Property(name = "sling.servlet.extensions", value = "json"),
        @Property(name = "sling.servlet.prefix", value = "-1", propertyPrivate = true)
})

public class ContentQueryServlet extends SlingSafeMethodsServlet
{

    private static final long serialVersionUID = 1L;

    private final Logger log = LoggerFactory.getLogger(ContentQueryServlet.class);

    /** Search clause */
    public static final String STATEMENT = "statement";

    /** Query type */
    public static final String QUERY_TYPE = "queryType";

    /** Result set offset */
    public static final String OFFSET = "offset";

    /** Number of rows requested */
    public static final String ROWS = "rows";

    /** property to append to the result */
    public static final String PROPERTY = "property";

    /** exerpt lookup path */
    public static final String EXCERPT_PATH = "excerptPath";

    /** rep:exerpt */
    private static final String REP_EXCERPT = "rep:excerpt()";

    @Property(value = "config/http/server")
    public static final String JCR_HTTP_SERVER_PATH = "jcr.http.server.path";

    private static String HTTP_SERVER_URL;

    public static final String TIDY = "tidy";

    private final JsonResourceWriter itemWriter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private SlingRepository repository;

    private Session session;

    public ContentQueryServlet()
    {
        itemWriter = new JsonResourceWriter(null);
    }

    /**
     * Read HTTP_SERVER_URL on bundle initialization
     *
     * @throws Exception
     */
    @Activate
    protected void activate(BundleContext bundleContext, Map<String, Object> props) throws Exception
    {
        session = repository.loginAdministrative(null);
        Object n = props.get(JCR_HTTP_SERVER_PATH);
        Node storageNode = session.getRootNode().getNode(n.toString());
        HTTP_SERVER_URL = storageNode.getProperty("httpUrl").getValue().getString();
    }

    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        if (session != null)
        {
            session.logout();
            session = null;
        }
    }

    /** True if our request wants the "tidy" pretty-printed format */
    protected boolean isTidy(SlingHttpServletRequest req)
    {
        for (String selector : req.getRequestPathInfo().getSelectors())
        {
            if (TIDY.equals(selector))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp) throws IOException
    {
        dumpResult(req, resp);
    }

    /**
     * Retrieve the query type from the request.
     *
     * @param req request
     *
     * @return the query type.
     */
    protected String getQueryType(SlingHttpServletRequest req)
    {
        return req.getParameter(QUERY_TYPE);
    }


    /**
     * Retrieve the query statement from the request.
     *
     * @param req       request
     * @param queryType the query type, as previously determined
     *
     * @return the query statement.
     */
    protected String getStatement(SlingHttpServletRequest req, String queryType)
    {
        return req.getParameter(STATEMENT);
    }

    /**
     * Dumps the result as JSON object.
     *
     * @param req  request
     * @param resp response
     *
     * @throws java.io.IOException in case the search will unexpectedly fail
     */
    private void dumpResult(SlingHttpServletRequest req, SlingHttpServletResponse resp) throws IOException
    {
        try
        {

            ResourceResolver resolver = req.getResourceResolver();

            String queryType = getQueryType(req);

            String statement = getStatement(req, queryType);

            statement = checkJcrRoot(req, statement);

            Iterator<Map<String, Object>> result = resolver.queryResources(
                    statement, queryType);


            if (req.getParameter(OFFSET) != null)
            {
                long skip = Long.parseLong(req.getParameter(OFFSET));
                while (skip > 0 && result.hasNext())
                {
                    result.next();
                    skip--;
                }
            }

            resp.setContentType(req.getResponseContentType());
            resp.setCharacterEncoding("UTF-8");

            final JSONWriter w = new JSONWriter(resp.getWriter());
            w.setTidy(isTidy(req));

            w.array();

            long count = -1;
            if (req.getParameter(ROWS) != null)
            {
                count = Long.parseLong(req.getParameter(ROWS));
            }

            List<String> properties = new ArrayList<String>();
            if (req.getParameterValues(PROPERTY) != null)
            {
                for (String property : req.getParameterValues(PROPERTY))
                {
                    properties.add(property);
                }
            }

            String exerptPath = "";
            if (req.getParameter(EXCERPT_PATH) != null)
            {
                exerptPath = req.getParameter(EXCERPT_PATH);
            }

            // iterate through the result set and build the "json result"
            while (result.hasNext() && count != 0)
            {
                Map<String, Object> row = result.next();

                w.object();
                String path = row.get("jcr:path").toString();

                w.key("name");
                w.value(ResourceUtil.getName(path));

                // Retrieve snapshotPath value from jcr
                Node storageNode = session.getRootNode().getNode(path.substring(1));
                boolean snapshotPropertyExists = storageNode.hasProperty("snapshotPath");
                if (snapshotPropertyExists)
                {
                    javax.jcr.Property snapshotProperty = storageNode.getProperty("snapshotPath");
                    String snapshotPath = snapshotProperty.getValue().getString();
                    w.key("snapshotUrl").value(HTTP_SERVER_URL + "/" + snapshotPath);
                }

                // dump columns
                for (String colName : row.keySet())
                {
                    w.key(colName);
                    String strValue = "";
                    if (colName.equals(REP_EXCERPT))
                    {
                        Object ev = row.get("rep:excerpt(" + exerptPath + ")");
                        strValue = (ev == null) ? "" : ev.toString();
                        w.value(strValue);

                    }
                    else
                    {
                        //strValue = formatValue(row.get(colName));
                        itemWriter.dumpValue(w, row.get(colName));
                    }
                    //w.value(strValue);
                }

                // load properties and add it to the result set
                if (!properties.isEmpty())
                {
                    Resource nodeRes = resolver.getResource(path);
                    dumpProperties(w, nodeRes, properties);
                }

                w.endObject();


                count--;
            }
            w.endArray();
        }
        catch (JSONException je)
        {
            throw wrapException(je);
        }
        catch (ValueFormatException e)
        {
            throw wrapException(e);
        }
        catch (PathNotFoundException e)
        {
            throw wrapException(e);
        }
        catch (RepositoryException e)
        {
            throw wrapException(e);
        }
    }

    private String checkJcrRoot(SlingHttpServletRequest req, String statement)
    {
        if (statement.indexOf("jcr:root") < 0)
        {
            statement = "/jcr:root" + req.getResource().getPath() + statement;
        }
        return statement;
    }

    private void dumpProperties(JSONWriter w, Resource nodeRes,
            List<String> properties) throws JSONException
    {

        // nothing to do if there is no resource
        if (nodeRes == null)
        {
            return;
        }

        itemWriter.dumpProperties(nodeRes, w, properties);

    }


    /**
     * @param e
     *
     * @throws org.apache.sling.api.SlingException
     *          wrapping the given exception
     */
    private SlingException wrapException(Exception e)
    {
        log.warn("Error in QueryServlet: " + e.toString(), e);
        return new SlingException(e.toString(), e);
    }


}
