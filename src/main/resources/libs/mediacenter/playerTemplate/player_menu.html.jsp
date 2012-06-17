<%@ page import="javax.annotation.Resource" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

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

<!-- Menu buttons -->
<div class="video_menu">
    <a id="library">Video Library</a>
    <a id="download">Download Video</a>
    <a id="embed" data-controls-modal="modal-embed">Embed Video</a>

    <!-- TODO: add a share/like button -->
    <%--<a id="contact" data-controls-modal="modal-contact">Contact</a>--%>
</div>

<!-- Library div overlay -->
<div class="video_library">
    <%
        String videoPath = resource.getPath() + ".widget_body.html";
        String channelPath = "";
        org.apache.sling.api.resource.Resource res = resource;

        Boolean found = false;
        while (found == false && res.getParent() != null)
        {

            if (res.getResourceType().equals("mediacenter:channel"))
            {
                found = true;
                channelPath = res.getPath();
            }
            res = res.getParent();
        }


        getServletConfig().getServletContext().setAttribute("channelPath", request.getContextPath() + channelPath);

        String videoLibraryPath = channelPath + ".widget_body.html";

    %>

    <!-- TODO: this is a hardcoded dependency; need to handle it/configure it elsewhere -->
    <!-- these extra modules to be included must be defined differently, so that if mediacenter/videoLibrary
        doesn't exists, the player looks ok -->
    <sling:include path="<%= videoLibraryPath %>" resourceType="mediacenter/videoLibrary"/>
</div>

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
            <%
                String embedURL = request.getRequestURL().toString().replaceAll("fplayer", "player").replaceAll("iplayer", "player");
            %>
            <iframe src="<%=embedURL%>"
                    scrolling="no" frameborder="0" vspace="0" hspace="0" marginwidth="0" marginheight="0"
                    height="430" width="640">
                <%=resource.getName()%>
            </iframe>
        </textarea>
    </div>
</div>