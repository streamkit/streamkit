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
        var snapshotPath = jsonObj.snapshotPath;
        var mediaPath = jsonObj.mediaPaths[0].mediaPath;
        var downloadPath = jsonObj.mediaPaths[0].downloadPath;

        var absoluteMediaPath = "http://" + streamUrl + mediaPath + "/playlist.m3u8";
        var absoluteSnapshotPath = snapshotPath;
        var absoluteMediaDownloadPath = downloadPath;


        // Add menu download link value
        $("#download").attr("href", absoluteMediaDownloadPath);

        var videoPlayerContainer = $("#video_player");
        videoPlayerContainer.html("<video id='video' width='100%' height='100%' poster='" + absoluteSnapshotPath + "' src='" + absoluteMediaPath + "' controls></video>");

        videoPlayerContainer.css("-webkit-transform-style", "preserve-3d");
        $("#video").css("-webkit-transform-style", "preserve-3d");

        // Bugfix for IDevices.
        // inspired by http://stackoverflow.com/questions/3683211/ipad-safari-mobile-seems-to-ignore-z-indexing-position-for-html5-video-elements

        $(".video_menu #library").click(function() {
//            console.log("Library button clicked");
//            videoPlayerContainer.css("-webkit-transform","translateX(-2048px)");
            $("#video").css({visibility: "hidden"});
            videoPlayerContainer.css({visibility: "hidden"});

        });

        $(".video_library #closeLibraryWidget").click(function()
        {
            $("#video").css({visibility: "visible"});
            videoPlayerContainer.css({visibility: "visible"});
//            videoPlayerContainer.css("-webkit-transform","translateX(0)");
        });

        // TODO: the click listeners above are ERROR-PRONE, Hard to detect. Must handle things differently
        // an alternative: when library is opened, library could add a CSS style on the <body>, "library-open"
        // based on library-open css style, we could alter the #video and #video_player container.


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

