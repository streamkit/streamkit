<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <title>
        Redirect client to i/f player
    </title>

 <!--
    <script type="text/javascript">
        $(document).ready(function() {

            // Read JSON with content information, parse it, and display the HMTL5 video tag
            var browserUrl = window.location.href;
            var jsonBrowserUrl = browserUrl.replace(".player.html", ".player.json");
            var manifest_f4m = browserUrl.replace(".player.html", ".player.f4m");
            var ajaxReturnJson = ajaxCall(jsonBrowserUrl);
            var jsonObj = jQuery.parseJSON(ajaxReturnJson);

            var snapshotPath = jsonObj.snapshotPath;
            var title = jsonObj.title;
            var description = jsonObj.description;

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
    <meta property="og:video:height" content="260" />
    <meta property="og:video:width" content="420" />
    <meta property="og:video:type" content="application/x-shockwave-flash" />
    <meta property="og:title" content="Big Buck Bunny" />
    <meta property="og:description" content="Big Buck Bunny is a short animated film by the Blender Institute, part of the Blender Foundation." />
    <meta property="og:image" content="http://www.example.com/bunny.png" />
    <meta property="og:video" content="http://fpdownload.adobe.com/strobe/FlashMediaPlayback.swf?src=" />
 -->

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
