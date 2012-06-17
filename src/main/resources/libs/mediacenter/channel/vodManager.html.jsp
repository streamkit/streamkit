<%@page session="false" %>
<%@page import="org.apache.sling.api.*" %>
<%@page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="org.osgi.framework.BundleContext" %>
<%@ page import="org.osgi.framework.FrameworkUtil" %>
<%@ page import="org.osgi.framework.ServiceReference" %>
<%@ page import="javax.jcr.Session" %>
<%@ page import="org.apache.sling.jcr.jackrabbit.accessmanager.PrivilegesInfo" %>
<%@ page import="javax.xml.soap.Node" %>
<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@ page import="org.mediacenter.auth.AccessControlUtil" %>

<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>


<%
    Boolean accessPrevented = AccessControlUtil.preventUnauthorizedAccess(slingRequest, response);
    if (accessPrevented == true)
    {
        return;
    }

    String listPath = resource.getPath() + ".list.html";
%>

<sling:include path="<%=listPath%>" resourceType="mediacenter/vod"></sling:include>