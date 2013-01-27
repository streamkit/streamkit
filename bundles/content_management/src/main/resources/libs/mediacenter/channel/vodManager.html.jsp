<%@page session="false" %>
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