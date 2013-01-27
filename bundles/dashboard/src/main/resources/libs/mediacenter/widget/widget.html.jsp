<%@page session="false" %>
<%@page import="org.apache.sling.api.*" %>
<%@page import="org.apache.sling.api.resource.Resource"%>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<%
   response.setHeader( "Pragma", "no-cache" );
   response.setHeader( "Cache-Control", "no-cache" );
   response.setDateHeader( "Expires", 0 );
%>

<%
    String widgetBody = resource.getPath() + ".widget_body.html";
    String forceResourceType = resource.getResourceType() + resource.getPath().substring( resource.getPath().lastIndexOf("/") );
//    String channelPath = request.getSession().getAttribute("channelPath").toString();
%>

<sling:include path="<%=widgetBody%>" resourceType="<%=forceResourceType%>" ></sling:include>

