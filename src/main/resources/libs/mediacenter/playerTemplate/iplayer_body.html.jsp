<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>


<script type="text/javascript">
    $(document).ready(function() {

        // Read JSON with content information, parse it, and display the HMTL5 video tag
        var browserUrl = window.location.href;
        var jsonBrowserUrl = browserUrl.replace(".iplayer.html", ".player.json");
        var ajaxReturnJson = ajaxCall(jsonBrowserUrl);
        var jsonObj = jQuery.parseJSON(ajaxReturnJson);
        var streamUrl = jsonObj.streamingServers[0].streamUrl;
        var httpServerUrl = jsonObj.httpServer.httpUrl;
        var snapshotPath = jsonObj.snapshotPath;
        var mediaPath = jsonObj.mediaPaths[0].mediaPath;

        var absoluteMediaPath = "http://" + streamUrl + mediaPath + "/playlist.m3u8";
        var absoluteSnapshotPath = httpServerUrl + "/" + snapshotPath;
        // alert(absoluteMediaPath);

        var videoPlayerContainer = $("#video_player");
        videoPlayerContainer.html("<video width='100%' height='100%' poster='" + absoluteSnapshotPath + "' src='" + absoluteMediaPath + "' controls></video>");

        // Bugfix for IDevices. Video container must be off screen when on overlay div appears
        $(".video_menu span").click(function() {
            videoPlayerContainer.css("-webkit-transform","translateX(-2048px)");
        });

        $(".close, #close").click(function() {
            videoPlayerContainer.css("-webkit-transform","translateX(0)");
        });


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

