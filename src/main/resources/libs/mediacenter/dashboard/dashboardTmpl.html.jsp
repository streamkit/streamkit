<%@page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>

    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script><![endif]-->

    <script type="text/javascript" src="/system/sling.js"></script>
    <%--<script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.min.js"></script>--%>
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.7.1.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/underscore-1.2.1.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/backbone-min.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/backbone.modelbinding.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/backbone/backbone.validation.js"></script>

    <%--<script type="text/javascript" src="<%= request.getContextPath() %>/js/mediacenter/commons-1.5-SNAPSHOT.js"></script>--%>
    <%--<script type="text/javascript" src="<%= request.getContextPath() %>/js/mediacenter/dashboard-1.5-SNAPSHOT.js"></script>--%>

    <title>LiveStreamKit Admin</title>
</head>
<body>

<link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap/bootstrap.min.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/mediacenter/dashboard.css">


<sling:include path="<%=resource.getPath().concat(\".dashboardHeader.html\")%>" resourceType="mediacenter/dashboard"
               replaceSuffix="dashboard"></sling:include>

<div class="page">
    <div class="page-container">
        <div class="container">
            <div class="row">


                <%--<div id="content" class="hero-unit page-content">--%>

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
                            interpolate: /{{(.+?)}}/g
                        };
                    </script>

                    <h2>Dashboard</h2>

                    <%
                        Object template = request.getAttribute("body.template");
                        Object options = request.getAttribute("body.template.options");
                    %>
                    <% if (template != null)
                    { %>
                    <% if (options == null)
                    { %>
                    <sling:include path="<%=template.toString()%>"/>
                    <% }
                    else
                    { %>
                    <% RequestDispatcherOptions rdo = new RequestDispatcherOptions(options.toString()); %>
                    <sling:include path="<%=template.toString()%>" resourceType="<%=rdo.getForceResourceType()%>"/>
                    <% } %>
                    <% } %>


                <%--</div>--%>

            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/bootstrap/bootstrap.js"></script>

</body>
</html>
