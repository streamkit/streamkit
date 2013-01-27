package org.mediacenter.auth;

import java.io.IOException;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.jcr.jackrabbit.accessmanager.PrivilegesInfo;

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * Util class providing utilities with respect to
 * access on admin areas.
 */
public class AccessControlUtil
{
    public static final String LOGIN_PATH = "/system/sling/login";   ///system/sling/selector/login

    public static final Boolean preventAnonymousAccess(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        String loginUrl = request.getContextPath() + LOGIN_PATH + "?resource=" + request.getPathInfo();
        if (request.getUserPrincipal() == null || "anonymous".equals(request.getRemoteUser()))
        {
            response.sendRedirect(loginUrl);
            return true;
        }
        return false;
    }

    public static final Boolean preventUnauthorizedAccess(SlingHttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        Boolean anonAccess = preventAnonymousAccess(request, response);
        if (anonAccess == true)
        {
            return true;
        }

        Node currentNode = request.getResource().adaptTo(Node.class);
        PrivilegesInfo privilegesInfo = new PrivilegesInfo();

        Boolean canModify = privilegesInfo.canAddChildren(currentNode) && privilegesInfo.canDeleteChildren(currentNode);

        if (!canModify)
        {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "You are not allowed to access this page");
            return true;
        }

        return false;
    }
}
