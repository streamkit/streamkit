<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>



<!-- TODO:// The download link should be generated on page init together with the entire content -->
<script type="text/javascript" src="http://watt.at/clientStrobe/lib/swfobject.js"></script>
<script type="text/javascript" src="http://watt.at/clientStrobe/lib/ParsedQueryString.js"></script>
<script type="text/javascript">
    $(document).ready(function() {

        // Read JSON with content information, parse it, and display the HMTL5 video tag
        var browserUrl = window.location.href;
        var jsonBrowserUrl = browserUrl.replace(".fplayer.html", ".player.json");
        var manifest_f4m = browserUrl.replace(".fplayer.html", ".player.f4m");
        var ajaxReturnJson = ajaxCall(jsonBrowserUrl);
        var jsonObj = jQuery.parseJSON(ajaxReturnJson);
        var downloadPath = jsonObj.downloadPath;
        var snapshotPath = jsonObj.snapshotPath;
        var duration = jsonObj.duration;

        var autoPlay = true;
        var streamType = "live";
        if (duration !== undefined) {
            autoPlay = false;
            streamType = "recorded";
        }

        var absoluteMediaDownloadPath = downloadPath;


        var absoluteSnapshotPath = snapshotPath;

        // Add menu download link value
        $("#download").attr("href", absoluteMediaDownloadPath);

        // Embed OSMF player
        var playerAbsoluteURL = "http://watt.at/clientStrobe";
        var pqs = new ParsedQueryString();
        var parameterNames = pqs.params(false);
        // Note that the buffer parameters below increase the OSMF defaults by 10 seconds
        var parameters = {
            src: manifest_f4m,
            autoPlay: autoPlay,
            poster: absoluteSnapshotPath,
            verbose: true,
            controlBarAutoHide: "true",
            controlBarPosition: "bottom",
            streamType: streamType,
            initialBufferTime: 2,
            expandedBufferTime: 7
        };

        for (var i = 0; i < parameterNames.length; i++) {
            var parameterName = parameterNames[i];
            parameters[parameterName] = pqs.param(parameterName) ||
            parameters[parameterName];
        }

        // Escape the ampersands so any URL params pass through OSMF into Wowza
        s = parameters['src'];
        s = escape(s);
        parameters['src'] = s;

        var wmodeValue = "direct";
        var wmodeOptions = ["direct", "opaque", "transparent", "window"];
        if (parameters.hasOwnProperty("wmode")) {
            if (wmodeOptions.indexOf(parameters.wmode) >= 0) {
                wmodeValue = parameters.wmode;
            }
            delete parameters.wmode;
        }

        // Embed the player SWF:
        swfobject.embedSWF(
                playerAbsoluteURL + "/StrobeMediaPlayback.swf"
                , "StrobeMediaPlayback"
                , "100%"
                , "100%"
                , "10.3.0"
                , playerAbsoluteURL + "/expressInstall.swf"
                , parameters
                , {
                    allowFullScreen: "true",
                    wmode: wmodeValue
                }
                , {
                    name: "StrobeMediaPlayback"
                }
        );

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

 <div id="video_player" class="video_player">
    <div id="StrobeMediaPlayback" style="background-color: #FFFFFF">In order to view this content you need the latest version of Adobe Flash Player. <a href="http://get.adobe.com/flashplayer/" target="_blank">Please click here to download.</a></div>
 </div>


