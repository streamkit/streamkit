<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<%
    String channelPath = (String)request.getSession().getAttribute("channelPath");
%>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <script type="text/javascript" src="/system/sling.js"></script>
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.7.1.js"></script>
    <!-- link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap/bootstrap.min.css"> -->

    <title>Live Preview Widget</title>

</head>
<body>

<script type="text/javascript">
    var runTimer = true;

    function getLiveResource() {
        var queryLiveContentSerach, channelPath;
        channelPath = "<%=channelPath%>";

        var currentChannelNode = Sling.getContent( channelPath , 1 );
        var channelName = currentChannelNode["title"];

        var liveContentJsonPath = channelPath + "/live/" + channelName;
        return liveContentJsonPath;
    }


    function activateIframe(liveContentPath) {
        // getting content live json, containg live viewer information
        var ajaxReturnJsonLive = ajaxCall(liveContentPath + ".player.json");
        var jsonObjLive = jQuery.parseJSON(ajaxReturnJsonLive);
        var mediaPathsObj = jsonObjLive.mediaPaths;

        // count users
        var connectionCounts = 0;
        for (var i = 0; i < mediaPathsObj.length; i++) {
            connectionCounts += mediaPathsObj[i].connectionCounts;
        }
        // display users
        var htmlStr = "Live broadcast is offline";
        var playerPath = liveContentPath + ".player.html";

        if (connectionCounts > 0) {
            htmlStr = "<iframe src='" + playerPath + "' scrolling='no' frameborder='0' vspace='0' hspace='0' marginwidth='0' marginheight='0' height='280' width='520'>Player</iframe>";
            runTimer = false;
        }

        $("#player").html(htmlStr);

    }

    function ajaxCall(url) {
        var ajaxValue = $.ajax ({
            url: url,
            type: 'GET',
            dataType: 'json',
            timeout: 5000,
            error: function(){
                alert('Internet connection error');
            },
            async: false
        }).responseText;
        return ajaxValue;
    }

    function timer() {
        var liveContentPath = getLiveResource();
        activateIframe(liveContentPath);
        setInterval(function(){
            // Stop running timer after IFrame has been displayed once
            if (runTimer) {
                liveContentPath = getLiveResource();
                activateIframe(liveContentPath);
            }
        }, 20000);
    }

    $(window).load( timer );

</script>


<div id="player"></div>

</body>
</html>


