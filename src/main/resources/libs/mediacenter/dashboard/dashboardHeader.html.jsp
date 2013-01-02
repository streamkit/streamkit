<%@page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>


<div id="in-nav">
    <div class="container">
        <div class="row">
            <div class="span12">
                <ul class="pull-right">
                    <li><a href="/system/sling/logout?resource=/content/channel/demo.dashboard.html">Logout</a></li>
                </ul>
                <a id="logo" href="<%=resource.getName()%>.dashboard.html">
                    <h4>My <strong>LiveStreamKit Admin</strong></h4></a>
            </div>
        </div>
    </div>
</div>

<div id="in-sub-nav">
    <div class="container">
        <div class="row">
            <div class="span12">
                <ul>
                    <li>
                        <a id="dashboard" href="<%=resource.getName()%>.dashboard.html"><i
                                class="batch home"></i><br>Dashboard</a>
                    </li>
                    <li>
                        <%--<span class="label label-important pull-right">08</span>--%>
                        <a id="vodManager" href="<%=resource.getName()%>.vodManager.html"><i
                                class="batch vodManager"></i><br>Manage Videos</a>
                    </li>
                    <li>
                        <%--<span class="label label-important pull-right">04</span>--%>
                        <a href="<%=resource.getName()%>.library.html"><i
                                class="batch library"></i><br>Video Gallery</a>
                    </li>
                    <%--<li><a href="calendar.html"><i class="batch calendar"></i><br>Calendar</a></li>--%>
                    <%--<li><a href="paragraphs.html"><i class="batch quill"></i><br>Paragraphs</a></li>--%>
                    <%--<li><a href="faq.html"><i class="batch forms"></i><br>FAQ</a></li>--%>
                    <%--<li><a href="settings.html"><i class="batch settings"></i><br>Settings</a></li>--%>
                </ul>
            </div>
        </div>
    </div>
</div>

<%
    String suffix = slingRequest.getRequestPathInfo().getSuffix();
    suffix = suffix.replaceAll("/", "");
%>

<script type="text/javascript">
    var activeDashboardHeader = "<%=suffix%>";
    $(window).load(function ()
            {
                if (activeDashboardHeader.length > 0)
                {
                    $("#" + activeDashboardHeader).addClass("active");
                }
            }
    )

</script>