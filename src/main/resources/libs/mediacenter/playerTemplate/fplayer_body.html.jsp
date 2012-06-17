<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<script type="text/javascript" src="/assets/player/js/swfobject.js"></script>
<%--<script type="text/javascript" src="/assets/player/js/history.js"></script>--%>
<script type="text/javascript">
    var application = "flash_player";
    var flash_version_major = "10.1.53";
    var flashvars = {
        contentPath: "<%=resource.getPath().concat(".player.json")%>",
        playerConfig: "<%=request.getContextPath()%>/assets/player/defaultPlayerConfiguration.xml"
    };
    var params = {
        menu: "false",
        scale: "noScale",
        allowScriptAccess:"sameDomain",
        allowFullScreen: "true",
        wmode: "opaque"
    };
    var attributes = {
        id: application,
        name: application
    };
    //To use express install, set to playerProductInstall.swf, otherwise the empty string.
    var xiSwfUrlStr = "";

    swfobject.embedSWF(
            "/assets/player/flex-main-sling-1.5-SNAPSHOT.swf?1.5-SNAPSHOT", "flash_player_div",
            "100%", "100%",
            flash_version_major, xiSwfUrlStr,
            flashvars, params, attributes);

    // JavaScript enabled so display the flashContent div in case it is not replaced with a swf object.
    swfobject.createCSS("#flashContent", "display:block;text-align:left;");
    /*swfobject.addLoadEvent(loadEventHandler);
     function loadEventHandler() {
     BrowserHistory.flexApplication = swfobject.getObjectById(application);
     }*/
</script>


<div class="video_player">
    <%--<object type="application/x-shockwave-flash" data="mediaelementjs/build/flashmediaelement.swf" width="100%" height="100%">--%>
    <%--<param name="allowfullscreen" value="true">--%>
    <%--<param name="allowscriptaccess" value="always">--%>
    <%--<param name="flashvars" value="controls=true&amp;file=../media/echo-hereweare.mp4" />--%>
    <%--<param name="wmode" value="transparent" />--%>
    <%--<!--[if IE]><param name="movie" value="player.swf"><![endif]-->--%>
    <%--<img src="bg_image/images.jpg" width="100%" height="100%" alt="Video">--%>
    <%--<p>Your browser can't play Flash videos. Please download the pluging.</p>--%>
    <%--</object>--%>
    <div id="flash_player_div">
        <p>This content requires the latest Adobe Flash Player version(10.1.53).
            <a href="http://get.adobe.com/flashplayer/" target="_blank">Get Flash</a>
            <br>
            Pentru a viziona aceasta transmisie aveti nevoie de ultima versiune de Adobe Flash Player (10.1.53).
            <a href="http://get.adobe.com/flashplayer/" target="_blank">Descarca Flash</a></p>
    </div>
</div>
