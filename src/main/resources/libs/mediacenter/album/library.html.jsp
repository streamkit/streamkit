<!-- standalone version of the video-library, loading the other mediacenter dependencies -->

<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>

    <script type="text/javascript" src="/system/sling.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/yepnope/yepnope.1.5.4.js"></script>

    <title>Video Library - Album <%= resource.getName() %></title>
    <!-- %=
            //Packages.java.text.MessageFormat.format(rb.getString("page.template.title"), title)
    % -->
</head>
<body>

<div id="videoLibrary">
    <span>Video Library</span>
    <%
        String widgetPath = resource.getPath() + ".widget_body.html";
    %>
    <h3><%=widgetPath%></h3>
    <sling:include path="<%= widgetPath %>" resourceType="mediacenter/videoLibrary" />

</div>

</body>
</html>

