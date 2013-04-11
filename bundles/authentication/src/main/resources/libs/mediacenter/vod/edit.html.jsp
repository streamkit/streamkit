<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>
<%
    //header options
    //request.setAttribute("page.title", rb.getString("page.title.create.channel"));

    request.setAttribute("body.template", resource.getPath() + ".edit_body.html");
    request.setAttribute("body.template.options", "forceResourceType= mediacenter/vod");

    //render the page using the default page template
    sling.include(resource.getPath() + ".pageTmpl.html", "forceResourceType= mediacenter/contentManager");
%>

<script>
    var init = function() {
        var vodModel = new VodModel();
        vodModel.contentPath = "<%= currentNode.getPath() %>";
        // the EditVodForm elements are loaded from edit_body.html.jsp
//        var editForm = new EditVodForm( {
//                                        el: $("#editVodFormContainer"),
//                                        template : $("#vodFormTemplate"),
//                                        model: vodModel
//                                        });
        var editForm = Backbone.View.Factory.createView( document.querySelector("#editVodFormContainer"), vodModel );
    }

    $(window).load( init );
</script>