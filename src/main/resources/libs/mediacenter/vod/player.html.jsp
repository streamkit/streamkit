<%@ page import="org.apache.sling.service.player.SocialMediaMetadata" %>
<%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<%
    String getVideoPath = SocialMediaMetadata.getVideoPath(slingRequest, response);
    String image = SocialMediaMetadata.getThumbnail(slingRequest, response);
    String title = SocialMediaMetadata.getTitle(slingRequest, response);
    String description = SocialMediaMetadata.getDescription(slingRequest, response);
%>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <title>
        Redirect client to i/f player
    </title>


    <meta property="og:type" content="movie" />
    <meta property="og:video:type" content="application/x-shockwave-flash" />
    <meta property="og:title" content="<%=title%>" />
    <meta property="og:description" content="<%=description%>" />
    <meta property="og:image" content="<%=image%>" />
    <meta property="og:video" content="<%=getVideoPath%>" />

    <script language="javascript" type="text/javascript">
        var browserUrl = window.location.href;
        if (navigator.userAgent.match(/iPad/i) != null || navigator.userAgent.match(/iPhone/i) != null || navigator.userAgent.match(/iPod/i) != null) {
            window.location = browserUrl.replace("player", "iplayer");
        } else {
            window.location = browserUrl.replace("player", "fplayer");
        }
    </script>

</head>
<body>

</body>
</html>
