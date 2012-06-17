<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <title>
        Redirect client to i/f player
    </title>

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
