<!-- small version of the video-library, assuming the other dependencies of the player are already loaded -->

<%@ page import="org.apache.sling.api.request.RequestDispatcherOptions" %>
<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<link rel="stylesheet" href="<%= request.getContextPath() %>/css/bootstrap/bootstrap.min.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/mediacenter/video-library.css">


<!-- Library div overlay -->
<div id="libraryWidget">
    <div class="libraryWidgetContainer">
        <div id="closeLibraryWidget">&times; Close</div>
        <form id="librarySearchFormContainer"
              data-component="VideoSearchForm"
              data-component-search-input-selector="#librarySearchInput">
            <fieldset>
                <div class="clearfix">

                    <div class="input-prepend">
                        <%--<span class="add-on"><i class="icon-search"></i></span>--%>

                        <input type="search" size="30" name="search" id="librarySearchInput" class="large search-query"
                               placeholder="search content">&nbsp;&nbsp;
                        <a href="#" class="btn secondary btn-success">
                            <i class="icon-search icon-white"></i>
                            Search
                        </a>
                    </div>

                </div>
            </fieldset>
        </form>
        <div id="librarySearchResultsContainer" class="searchResults"
             data-component="VideoResultsForm"
             data-component-item-renderer-template="\\${jQuery('#videoItem').html()}">

            <a id="libraryLoadingAnimation" class="loading">
                <div>
                    <img style="background-image: url('<%= request.getContextPath() %>/img/loading_white_transparent.gif');"/>
                    <p>Loading ...</p>
                </div>
            </a>
        </div>
    </div>
</div>


<script type="text/javascript">
    window.videoLibrary = window.videoLibrary || {};

    window.videoLibrary.completeCount = 0;

    window.videoLibrary.logger = function (msg)
    {
        if (typeof console != "undefined" && console != null)
        {
            console.log(msg);
        }
    }

    window.videoLibrary.initLibrary = function ()
    {
        window.videoLibrary.completeCount++;
        if (window.videoLibrary.completeCount < 6)
        {
            return;
        }

        var _searchModel, _searchForm, _searchResults, _channelPath;
        _searchModel = new VideoSearchModel();

        // try to see if you are in a specific context first
        _channelPath = "<%=getServletConfig().getServletContext().getAttribute("channelPath")%>";

        if (_channelPath === "null")
        {
            _channelPath = Sling.currentPath;
        }
        _searchModel.setChannelPath(_channelPath);

        _searchForm = Backbone.View.Factory.createView( jQuery("#librarySearchFormContainer")[0], _searchModel);
        _searchResults = Backbone.View.Factory.createView( jQuery("#librarySearchResultsContainer")[0], _searchModel);
    }

    yepnope([
        {
            test: window.jQuery,
            nope: ['<%= request.getContextPath() %>/js/jquery-1.7.1.min.js'],
            complete: function()
            {
                window.videoLibrary.logger("jQuery dependency has been loaded");
                window.videoLibrary.initLibrary();

                var _backboneDef = {
                    test: window.Backbone,
                    nope: ['<%= request.getContextPath() %>/js/backbone/underscore-1.2.1.js',
                        '<%= request.getContextPath() %>/js/backbone/backbone-min.js'],
                    complete: function()
                    {
                        window.videoLibrary.logger("Backbone dependency has been loaded");
                        _.templateSettings = {
                            interpolate : /{{(.+?)}}/g
                        };
                        window.videoLibrary.initLibrary();
                    }
                };
                var _bootstrapDef = {
                    test: jQuery.fn.popover, // one of Bootstrap's jQ plugins
                    nope:['<%= request.getContextPath() %>/js/bootstrap/bootstrap.js'],
                    complete: function()
                    {
                        window.videoLibrary.logger("Bootstrap dependency has been loaded");
                        window.videoLibrary.initLibrary();
                    }
                };

                var _videoLibraryDef = {
                    test: window.VideoSearchForm, // should be a custom namespace: window.videoLibrary
                    nope:['<%= request.getContextPath() %>/js/mediacenter/commons-${commons.version}.js',
                        '<%= request.getContextPath() %>/js/mediacenter/video-library-${project.version}.js'],
                    complete: function ()
                    {
                        window.videoLibrary.logger("VideoLibrary dependency has been loaded");
                        window.videoLibrary.initLibrary();
                    }
                };

                var _iListDef = {
                    test: window.iList,
                    nope:['<%= request.getContextPath() %>/js/infinite-list/infinite-list-1.1-SNAPSHOT.js'],
                    complete: function()
                    {
                        window.videoLibrary.logger("Infinite List dependency has been loaded");
                        window.videoLibrary.initLibrary();
                    }
                };

                var _timeAgoDef = {
                    test: jQuery.timeago,
                    nope:['<%= request.getContextPath() %>/js/timeago/jquery.timeago.js'],
                    complete: function()
                    {
                        window.videoLibrary.logger("jQeury.timeago dependency has been loaded");
                        window.videoLibrary.initLibrary();
                    }
                }

                yepnope([
                    _backboneDef,
                    _bootstrapDef,
                    _iListDef,
                    _timeAgoDef,
                    _videoLibraryDef ]); // after loading jQuery, load backbone and bootstrap
            }
        }
    ])
</script>

<!-- templates -->

<script id="videoItem" type="text/template">
    <a href="{{path}}.player.html">
        <div>
            <img style="background-image:url({{element.snapshotUrl}});"/>
            <!-- using timeago plugin from http://timeago.yarp.com/ -->
            <time class="time-ago" datetime="{{created}}">{{created}}</time>
            <p>{{element.title}}</p>
        </div>
    </a>
</script>
