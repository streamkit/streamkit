<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>
<%
    //header options
    request.setAttribute("page.title", "IPlayer Media Center");

    request.setAttribute("body.player", resource.getPath() + ".iplayer_body.html");

    String suffix = slingRequest.getRequestPathInfo().getSuffix();
    if( suffix != null && suffix.startsWith("/menu")) {
        request.setAttribute("menu.player", resource.getPath() + ".player_menu.html");
    }

    request.setAttribute("body.options", "forceResourceType= mediacenter/playerTemplate");

    //render the page using the default page template
    sling.include(resource.getPath() + ".playerTmpl.html", "forceResourceType= mediacenter/playerTemplate");
%>