<%@ page import="javax.annotation.Resource" %>
<%@ page import="javax.jcr.Node" %>
<%@ page import="org.mediacenter.resource.ChannelNodeLookup" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>
<%
    String embedURL = request.getRequestURL().toString().replaceAll("fplayer", "player").replaceAll("iplayer", "player");
%>

<!-- Menu buttons -->
<div class="video_menu">
    <a id="library">Video Library</a>
    <a id="download" target="_black">Download Video</a>
    <a id="embed" data-controls-modal="modal-embed">Embed Video</a>

    <div id="fb-root" style="display: none"></div>
    <script>(function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s); js.id = id;
        js.src = "http://connect.facebook.net/en_US/all.js#xfbml=1&appId=113796565354147";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));</script>

    <div class="fb-like" data-href="<%=embedURL%>" data-send="false" data-layout="button_count" data-width="450" data-show-faces="true"></div>

    <!-- TODO: add a share/like button -->
    <%--<a id="contact" data-controls-modal="modal-contact">Contact</a>--%>
</div>

<!-- Library div overlay -->
<div class="video_library">
    <%
        String videoLibraryWidgetPath = "";
        String videoLibraryPath = "";

        Node currentChannel = ChannelNodeLookup.getClosestChannelInNode(resource.adaptTo(Node.class));
        Node currentAlbum = ChannelNodeLookup.getClosestAlbumInPath( resource.adaptTo(Node.class) );

        String channelNodePath = "";
        if ( currentChannel != null )
        {
            channelNodePath = currentChannel.getPath();
            videoLibraryWidgetPath = channelNodePath + ".widget_body.html";
            videoLibraryPath = channelNodePath;
        }

        String albumNodePath = "";
        if ( currentAlbum != null )
        {
            albumNodePath = currentAlbum.getPath();
            videoLibraryWidgetPath = albumNodePath + ".widget_body.html";
            videoLibraryPath = albumNodePath;
        }
    %>


    <!-- TODO: this is a hardcoded dependency; need to handle it/configure it elsewhere -->
    <!-- these extra modules to be included must be defined differently, so that if mediacenter/videoLibrary
        doesn't exists, the player looks ok -->
    <sling:include path="<%= videoLibraryWidgetPath %>" resourceType="mediacenter/videoLibrary" />
</div>

<script type="text/javascript">
    $(document).ready(function()
    {
        var library = $(".video_library");
        var contact = $(".channel_contact");
        $(".video_menu #library").click(function()
        {
            library.fadeToggle(300);
        });
        $(".video_library #closeLibraryWidget").click(function()
        {
            library.fadeToggle(300);
        });

    });
</script>

<!--[if lt IE 9]>
<script type="text/javascript">
    window.videoLibraryPath = "<%=videoLibraryPath%>.library.html";
    $(document).ready(function()
    {
        var library = $(".video_library");
        var contact = $(".channel_contact");
        $(".video_menu #library").click(function()
        {
            window.location.href = window.videoLibraryPath;
        });
        $(".video_library #closeLibraryWidget").click(function()
        {
            // nothing to do
            // window.history.go( -1 );
        });

    });
</script>
<![endif]-->


<!-- Block containing the Contact form -->
<div id="modal-contact" class="modal hide fade">
    <div class="modal-header">
        <a href="#" class="close">&times;</a>

        <h3>Contact form</h3>
    </div>
    <div class="modal-body">
        <form>
            <fieldset>
                <legend>Fill your email address and message</legend>
                <div class="clearfix">
                    <label for="email">Email:</label>

                    <div class="input">
                        <input type="text" size="30" id="email" name="email" id="xlInput" class="xlarge"
                               placeholder="john.doe@email.com">
                    </div>
                </div>
                <div class="clearfix">
                    <label for="message">Message:</label>

                    <div class="input">
                        <textarea rows="3" name="message" id="message" class="xlarge"
                                  placeholder="Your message to channel owner here"></textarea>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
    <div class="modal-footer">
        <a href="#" class="btn primary">Send email</a>
        <a href="#" class="btn secondary">Cancel</a>
    </div>
</div>

<!-- Block containing the Embed form -->
<div id="modal-embed" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>

        <h3>Embed Video</h3>
    </div>
    <div class="modal-body">

        <h4>Select, copy and paste the code below</h4>

        <textarea rows="3" name="code" id="code" style="height: 150px; width: 100%">
<iframe src="<%=embedURL%>" scrolling="no" frameborder="0" vspace="0" hspace="0" marginwidth="0" marginheight="0" height="430" width="640">
    <%=resource.getName()%>
</iframe>
        </textarea>
    </div>
</div>
