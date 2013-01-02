<%@page session="false" %>
<%@ page import="org.mediacenter.auth.AccessControlUtil" %>
<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<%

    Boolean accessPrevented = AccessControlUtil.preventUnauthorizedAccess(slingRequest, response);
    if (accessPrevented == true)
    {
        return;
    }

    //header options
    //request.setAttribute("page.title", rb.getString("page.title.create.channel"));

    request.setAttribute("body.template", resource.getPath() + ".gridLayoutDashboard.html");
    request.setAttribute("body.template.options", "forceResourceType= mediacenter/dashboard");

    //render the page using the default page template
    String pageTemplate = resource.getPath() + ".dashboardTmpl.html";
%>

<sling:include path="<%=pageTemplate%>" resourceType="mediacenter/dashboard" flush="true"></sling:include>