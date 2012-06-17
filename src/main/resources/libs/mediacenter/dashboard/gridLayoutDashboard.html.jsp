<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<span>GRID LAYOUT DASHBOARD SAMPLE WITH MASONRY LAYOUT</span>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/masonry/jquery.masonry.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/mediacenter/masonry.css">

<div id="widgets-container" class="" style="width: 90%; position: absolute">

</div>

<script id="widgetTemplate" type="text/template">
    <div class="widget span{{widget.spanX}} heightSpan{{widget.spanY}}">
        <div class="widget-body span{{widget.spanX}} heightSpan{{widget.spanY}}">
            <div class="widget-header">{{widget.title}}</div>
            <div class="widget-content">
                <iframe frameborder="0" align="middle" scrolling="auto" src="{{widget.widgetUrl}}"
                        style="height: 100%; width: 100%;"/>
            </div>
        </div>
    </div>
</script>

<script type="text/javascript">
    var contextPath = '<%= request.getContextPath() %>';

    function init() {
        // getting user's bundles
        var query, channelPath;
        channelPath = "<%=resource.getPath()%>"

        // getting al bundles
        query = channelPath + ".widget_query.tidy.json?queryType=xpath&statement=//*";
        query += "[sling:resourceType='mediacenter:widget']&property=title&property=grid_size&property=active";
        $.getJSON(query, allWidgetsLoadCallback);

    }

    function allWidgetsLoadCallback(data) {
        var w;
        w = data[0];
        w.grid_size = "8x4";
        displayWidget(w);
        w.grid_size = "4x2";
        displayWidget(w);
        w.grid_size = "4x2";
        displayWidget(w);
        w.grid_size = "4x2";
        displayWidget(w);
        w.grid_size = "4x2";
        displayWidget(w);
        w.grid_size = "4x4";
        displayWidget(w);
        w.grid_size = "8x4";
        displayWidget(w);
        w.grid_size = "4x4";
        displayWidget(w);
        w.grid_size = "8x4";
        displayWidget(w);
        w.grid_size = "4x2";
        displayWidget(w);

        $.each(data, function (index, widget)
        {
//            widget.grid_size = "2x2";
//            displayWidget(widget);
//            widget.grid_size = "3x2";
//            displayWidget(widget);
//            widget.grid_size = "2x2";
//            displayWidget(widget);
//            widget.grid_size = "4x4";
//            displayWidget(widget);
//
//            widget.grid_size = "7x4";
//            displayWidget(widget);
//
//            widget.grid_size = "4x2";
//            displayWidget(widget);
//
//            widget.grid_size = "5x2";
//            displayWidget(widget);
//
//            widget.grid_size = "4x2";
//            displayWidget(widget);
//
//            widget.grid_size = "5x2";
//            displayWidget(widget);
//            widget.grid_size = "2x2";
//            displayWidget(widget);
//            widget.grid_size = "3x2";
            displayWidget(widget);
        });

        $('#widgets-container').masonry({
            itemSelector: '.widget',
            columnWidth: 20,
            isAnimated: true
        });
    }

    function displayWidget(widget) {
        var widgetGridLayout, spanX, spanY;
        widgetGridLayout = widget.grid_size;
        spanX = widgetGridLayout.split("x")[0]
        spanY = widgetGridLayout.split("x")[1]
        widget.widgetUrl = contextPath + widget['jcr:path'] + ".widget.html";
        widget.spanX = spanX;
        widget.spanY = spanY;

        var el = _.template( $("#widgetTemplate").html(), {widget:widget});
        $("#widgets-container").append(el);
//        el.data("config", widget);
    }

    $(window).load( init );

</script>