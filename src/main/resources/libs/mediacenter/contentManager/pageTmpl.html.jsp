<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <script type="text/javascript" src="/system/sling.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-1.7.1.js"></script>
    <%--<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.1.js"></script>--%>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/underscore-1.4.0.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/backbone-0.9.2.js"></script>
    <%--<script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/backbone.modelbinding.js"></script>--%>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/rivets/rivets-0.3.10.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/backbone.validation-0.6.2.js"></script>

    <script type="text/javascript" src="<%= request.getContextPath() %>/js/mediacenter/commons-${commons.version}.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/mediacenter/content_management-${project.version}.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/infinite-list/infinite-list-1.1-SNAPSHOT.js"></script>

    <title>Content Manager</title>
    <!-- %=
            //Packages.java.text.MessageFormat.format(rb.getString("page.template.title"), title)
    % -->
</head>
<body>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/contentManager.css">

    <div id="content" class="container hero-unit page-content">
        <noscript>
            <div class="ui-widget" style="margin-bottom: 10px;">
                <div style="padding: 0pt 0.7em;" class="ui-state-error ui-corner-all">
                    <p><span style="float: left; margin-right: 0.3em;" class="ui-icon ui-icon-alert"></span>
                    <!-- %= // rb.getString("msg.noscript")
                    % --></p>
                </div>
            </div>
        </noscript>

    <script>
        _.templateSettings = {
            //interpolate : /\\$\{(.+?)\}/g
            interpolate : /{{(.+?)}}/g
        };
    </script>

    <h2>Content Manager</h2>

    <%
        Object template = request.getAttribute("body.template");
        Object options = request.getAttribute("body.template.options");
    %>
        <% if ( template != null ) { %>
            <% if ( options == null ) { %>
                <sling:include path="<%=template.toString()%>" />
            <% } else { %>
                <% RequestDispatcherOptions rdo = new RequestDispatcherOptions(options.toString());  %>
                <sling:include path="<%=template.toString()%>" resourceType="<%=rdo.getForceResourceType()%>" />
            <% } %>
        <% } %>


    </div>

    <script type="text/javascript" src="<%= request.getContextPath() %>/js/bootstrap/bootstrap.js"></script>

</body>
</html>
