<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>



<!-- TODO:// The download link should be generated on page init together with the entire content -->
<script type="text/javascript">
    $(document).ready(function() {

        // Read JSON with content information, parse it, and display the HMTL5 video tag
        var browserUrl = window.location.href;
        var jsonBrowserUrl = browserUrl.replace(".fplayer.html", ".player.json");
        var ajaxReturnJson = ajaxCall(jsonBrowserUrl);
        var jsonObj = jQuery.parseJSON(ajaxReturnJson);
        var downloadPath = jsonObj.mediaPaths[0].downloadPath;
        var snapshotPath = jsonObj.snapshotPath;
        var duration = jsonObj.duration;

        var autoPlay = true;
        if (duration !== undefined) {
            autoPlay = false;
        }

        var absoluteMediaDownloadPath = downloadPath;

        var streamUrl = jsonObj.streamingServers[0].streamUrl;
        var mediaPath = jsonObj.mediaPaths[0].mediaPath;
        var absoluteMediaPath = streamUrl + mediaPath + "/manifest.f4m";
        var absoluteSnapshotPath = snapshotPath;

        // Add menu download link value
        $("#download").attr("href", absoluteMediaDownloadPath);

        var videoPlayerContainer = $("#video_player");
        videoPlayerContainer.html("<object width='100%' height='100%'>" +
                "<param name='movie' value='http://fpdownload.adobe.com/strobe/FlashMediaPlayback_101.swf'></param>" +
                "<param name='flashvars' value='src=" + absoluteMediaPath + "&autoPlay=" + autoPlay + "&poster=" + absoluteSnapshotPath + "'>" +
                "</param><param name='allowFullScreen' value='true'></param><param name='allowscriptaccess' value='always'>" +
                "</param><embed src='http://fpdownload.adobe.com/strobe/FlashMediaPlayback_101.swf' " +
                "type='application/x-shockwave-flash' allowscriptaccess='always' allowfullscreen='true' " +
                "width='100%' height='100%' flashvars='src=" + absoluteMediaPath + "&autoPlay=" + autoPlay + "&poster=" + absoluteSnapshotPath + "'>" +
                "</embed></object>");


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



<div id="video_player" class="video_player" >Your browser does not support HTML5 video tags.</div>



