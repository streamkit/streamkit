<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<%
    String channelPath = (String)request.getSession().getAttribute("channelPath");
%>

<span style="font-size: 12px; font-family: Helvetica Neue,Helvetica,Arial,sans-serif;"><a href="<%=channelPath%>.vodManager.html" target="_parent">Content list</a></span>