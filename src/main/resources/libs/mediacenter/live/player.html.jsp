<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <title>
        Redirect client to i/f player
    </title>


    <meta property="og:type" content="movie" />
    <meta property="og:video:height" content="260" />
    <meta property="og:video:width" content="420" />
    <meta property="og:video:type" content="application/x-shockwave-flash" />
    <meta property="og:title" content="Big Buck Bunny" />
    <meta property="og:description" content="Big Buck Bunny is a short animated film by the Blender Institute, part of the Blender Foundation." />
    <meta property="og:image" content="http://space.crestin.tv/hdd_download_crestintv/content/2/image/Concert_Filarmonica_Timisoara_20120819085405115.jpg" />
    <meta property="og:video" content="http://watt.at/clientStrobe/StrobeMediaPlayback.swf?src=http://mcdev.crestin.tv/content/channel/adventist_at/vod/2012/08/Concert%20Filarmonica%20Timisoara.player.f4m&plugin_cdn=http://watt.at/clientStrobe/StrobeCDNPlugin.swf&autoPlay=true&poster=http://space.crestin.tv/hdd_download_crestintv/content/2/image/Concert_Filarmonica_Timisoara_20120819085405115.jpg&streamType=recorded" />

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
