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

    <title>Live stats</title>

</head>
<body>

<!-- Display online viewers -->
<div id="stats-container">
    Live viewers: <br>
    <div id="online-viewers" style="font-size: 5em; font-weight: bold;"></div>
</div>


<script type="text/javascript">

    function getLiveResource() {
        var queryLiveContentSerach, channelPath;
        channelPath = "<%=channelPath%>";

        // getting channel's live content
        queryLiveContentSerach = channelPath + ".query.tidy.json?queryType=xpath&statement=//*";
        queryLiveContentSerach += "[sling:resourceType='mediacenter:live']";

        var ajaxReturnJsonLivePath = ajaxCall(queryLiveContentSerach);
        var jsonObjLivePath = jQuery.parseJSON(ajaxReturnJsonLivePath);
        var liveContentPath = jsonObjLivePath[0]['jcr:path'];

        return liveContentPath;
    }


    function getCurrentUsers(liveContentPath) {
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
        $("#online-viewers").html(connectionCounts);

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
        getCurrentUsers(liveContentPath);
        setInterval(function(){
            getCurrentUsers(liveContentPath);
        }, 20000);
    }

    $(window).load( timer );

</script>

</body>
</html>
