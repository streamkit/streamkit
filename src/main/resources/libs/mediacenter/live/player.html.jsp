<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <script src="<%= request.getContextPath() %>/js/jquery-1.7.1.min.js"></script>

    <title>
        Redirect client to i/f player
    </title>

    <script type="text/javascript">
        $(document).ready(function() {

            // Read JSON with content information, parse it, and display the HMTL5 video tag
            var browserUrl = window.location.href;
            var jsonBrowserUrl = browserUrl.replace(".player.html", ".player.json");
            var manifest_f4m = browserUrl.replace(".player.html", ".player.f4m").replace(new RegExp("/menu" + '$'), "");
            var ajaxReturnJson = ajaxCall(jsonBrowserUrl);
            var jsonObj = jQuery.parseJSON(ajaxReturnJson);
            var snapshotPath = "http://us.resiliencesystem.org/sites/default/files/u257/livestream.jpg";
            var title = "Live";
            var description = "Live streaming";
            var duration = jsonObj.duration;

            var streamType = "live";
            if (duration !== undefined) {
                streamType = "recorded";
            }

            // Embed OSMF player
            var playerAbsoluteURL = "http://watt.at/clientStrobe";

            var videoPath = "http://watt.at/clientStrobe/StrobeMediaPlayback.swf" + "?" +
                    "plugin_cdn=http://watt.at/clientStrobe/StrobeCDNPlugin.swf" + "&" +
                    "src=" + manifest_f4m + "&" +
                    "autoPlay=true" + "&" +
                    "controlBarAutoHide=true" + "&" +
                    "controlBarPosition=bottom" + "&" +
                    "streamType=" + streamType + "&" +
                    "initialBufferTime=2" + "&" +
                    "expandedBufferTime=7";

            $('meta[property="og:title"]').attr("content", title);
            $('meta[property="og:description"]').attr("content", description);
            $('meta[property="og:image"]').attr("content", snapshotPath);
            $('meta[property="og:video"]').attr("content", videoPath);

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
        });
    </script>

    <meta property="og:type" content="movie" />
    <meta property="og:video:type" content="application/x-shockwave-flash" />
    <meta property="og:title" content="" />
    <meta property="og:description" content="" />
    <meta property="og:image" content="" />
    <meta property="og:video" content="" />

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
