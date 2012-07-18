<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<%
    String videoName = resource.getName();
%>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <title>
        Playing <%=videoName%>
    </title>

    <!-- %=
            //Packages.java.text.MessageFormat.format(rb.getString("page.template.title"), title)
        % -->

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script type="text/javascript">
        if (typeof jQuery == 'undefined')
        {
            document.write('\<script src="<%= request.getContextPath() %>/js/jquery-1.7.1.min.js"\>\<\/script\>');
        }
    </script>

    <script src="http://twitter.github.com/bootstrap/1.4.0/bootstrap-modal.js"></script>
    <link href="http://twitter.github.com/bootstrap/1.4.0/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/vod/css/public_player.css" media="screen" rel="stylesheet">

    <script type="text/javascript" src="<%= request.getContextPath() %>/system/sling.js"></script>

    <script type="text/javascript" src="<%= request.getContextPath() %>/js/yepnope/yepnope.1.5.4.js"></script>

    <!--[if lt IE 9]>
        <script src="<%= request.getContextPath() %>/assets/polyfills/html5shiv.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/assets/polyfills/selectivizr-min.js"></script>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/vod/css/public_player.ie.css" />
    <![endif]-->

</head>
<body>

    <!-- Player include -->
    <%
        Object body_fplayer = request.getAttribute("body.player");
        Object menu_player = request.getAttribute("menu.player");
        Object body_options = request.getAttribute("body.options");
    %>
        <% if ( body_fplayer != null ) { %>
            <% if ( body_options == null ) { %>
                <sling:include path="<%=body_fplayer.toString()%>" />
            <% } else { %>
                <% RequestDispatcherOptions rdo = new RequestDispatcherOptions(body_options.toString());  %>
                <sling:include path="<%=body_fplayer.toString()%>" resourceType="<%=rdo.getForceResourceType()%>" />
            <% } %>
        <% } %>


    <!-- Player menu -->
    <% if ( menu_player != null ) { %>
        <% if ( body_options == null ) { %>
            <sling:include path="<%=menu_player.toString()%>" />
        <% } else { %>
        <% RequestDispatcherOptions rdo = new RequestDispatcherOptions(body_options.toString());  %>
            <sling:include path="<%=menu_player.toString()%>" resourceType="<%=rdo.getForceResourceType()%>" />
        <% } %>
    <% } %>


 <!-- Analytics -->
 <script type="text/javascript"> var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www."); document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E")); </script> <script type="text/javascript"> var pageTracker = _gat._getTracker("UA-3227716-7"); pageTracker._trackPageview(); </script>
</body>
</html>
