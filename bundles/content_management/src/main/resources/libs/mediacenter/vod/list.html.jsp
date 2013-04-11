<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>
<%
    //header options
    //request.setAttribute("page.title", rb.getString("page.title.create.channel"));

    request.setAttribute("body.template", resource.getPath() + ".list_body.html");
    request.setAttribute("body.template.options", "forceResourceType= mediacenter/vod");

    //render the page using the default page template
    String pageTemplate = resource.getPath() + ".pageTmpl.html";

%>

<sling:include path="<%=pageTemplate%>" resourceType="mediacenter/contentManager" flush="true"></sling:include>

