<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<sling:include path="<%=resource.getPath().concat(".edit_body.html")%>" resourceType="mediacenter/vod"/>

<!-- PAGE DIALOGS -->
<div id="modal-vod-from" class="modal hide fade" style="display: block; ">
    <div class="modal-header">
        <a href="#" class="close">&times;</a>

        <h3>Upload to Video Library</h3>
    </div>
    <div id="editFormContainer" class="modal-body"
         data-component="EditVodForm"
         data-component-template="\${document.querySelector('#vodFormTemplate')}">
        <p>Create VOD body TBD ...</p>

    </div>
    <div class="modal-footer">
        <a href="#" id="saveVodButton" class="btn btn-primary">Upload</a>
        <a href="#" id="cancelVodButton" class="btn" data-dismiss="modal">Cancel</a>
    </div>
</div>

<div id="modal-delete-confirmation" class="modal hide fade">
    <div class="modal-header">
        <a href="#" class="close">&times;</a>

        <h3>Are you sure you want to delete this content ?</h3>
    </div>
    <div class="modal-body">
        <p>The information about the video along with the video file(s) will be permanently deleted.</p>
    </div>
    <div class="modal-footer">
        <a href="#" id="deleteVodButton" class="btn btn-primary">Delete</a>
        <a href="#" id="cancelDeleteVodButton" class="btn" data-dismiss="modal">Cancel</a>
    </div>
</div>

<!-- PAGE LAYOUT -->
<div class="row">
    <div class="span8">
        <!-- this button will eventually show the #modal-vod-form dialog -->
        <button id="addNewButton"
                data-backdrop="true" data-keyboard="true" class="btn btn-primary">Add New
        </button>
    </div>
    <div id="searchFormContainer" class="span4"
         data-component="VodSearchForm">
        <form method="get" class="form-search" style=" float: right; ">
            <input id="searchInput" type="search" name="q"
                   placeHolder="Search video..."
                   class="input-medium search-query" size="30"/>
        </form>
    </div>
</div>

<div class="row">
    <div id="searchResultsFormContainer" class="span16"
         data-component="VodResultsForm"
         data-component-template="\${document.querySelector('#searchResultsTemplate')}"
         data-component-item-renderer-template="\${document.querySelector('#vodItemRendererTemplate')}">


    </div>
</div>

<div id="uploadContainer" class="uploadContainer"
     data-component="UploadContainer"
     data-component-template="\${document.querySelector('#uploadContainerTemplate')}"
     data-component-item-renderer-template="\${document.querySelector('#uploadControlBarTemplate')}">
    <span>Upload status</span>
</div>

<!-- BACKBONE TEMPLATES -->

<!-- Table containing the results -->
<script id="searchResultsTemplate" type="text/template">
    <div class="row">
        <table id="contentTable">
            <theader>
                <tr>
                    <th> Name</th>
                    <th> Edit</th>
                    <th> Delete</th>
                    <th> Flash Player</th>
                    <th> HTML Player</th>
                </tr>
            </theader>
            <tbody id="searchResultsTableBody" class="video-list">
                <tr id="listLoader" class="loading">
                    <td colspan="5">
                        <img src="<%= request.getContextPath() %>/img/loading.gif"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</script>

<script id="vodItemRendererTemplate" type="text/template">
    <tr>
        <td width="90%">{{element.title}}</td>
        <td><a href="{{path}}.edit.html">
            <button data-content-path="{{path}}" name="editContentButton" class="btn btn-primary">Edit</button>
        </a></td>
        <td>
            <button name="deleteContentButton"
                    data-toggle="modal"
                    class="btn"
                    data-content-path="{{path}}">Delete
            </button>
        </td>
        <td>
            <a href="{{path}}.fplayer.html" target="_blank">Play</a>
        </td>
        <td>
            <a href="{{path}}.iplayer.html" target="_blank">Play</a>
        </td>
    </tr>
</script>

<script id="uploadContainerTemplate" type="text/template">
    <ul class="uploadsList">

    </ul>
</script>

<script id="uploadControlBarTemplate" type="text/template">
    <!-- NOTE: this is going to be wrapped into a <li class="uploadRenderer"> -->
    <div class="modal-header">
        <a href="#" class="close closeButton">x</a>

        <div id="uploadTitle" class="uploadTitle" data-text="model.title"></div>
    </div>
    <div class="modal-footer">
        <ul data-component="MessageBox"
            data-component-message-template="\${document.querySelector('#uploadCtrlBarMsgTemplate').textContent}">
            <!-- placeholder to show success/error messages after upload -->
        </ul>
        <progress id="uploadProgressBar" class="uploadProgressBar"
                  data-value="model.progress" data-text="model.progress"
                  min="0" max="100" value="0"/>

        <!-- TODO: <div class="uploadSpeed">Upload speed: uploadKBs KiB/s </div> -->
        <div data-text="model.progress">0%</div>
    </div>
</script>

<script id="uploadCtrlBarMsgTemplate" type="text/template">
    <li class="alert alert-{{type}}">
        <a class="close" data-dismiss="alert">x</a>
        {{message}}
    </li>
</script>

<script type="text/javascript">
    var init = function()
    {
        var searchModel, searchForm, activeUploads, requestQuery;
        var vodModel;
        /* : VodModel */
        // the EditVodForm elements are loaded from edit_body.html.jsp
        var editForm;
        /* : EditVodForm */

        ContentManagerContext.getInstance().init();
        activeUploads = ContentManagerContext.getInstance().getActiveUploads();
        searchModel = ContentManagerContext.getInstance().getSearchModel();

        searchModel.setChannelPath(Sling.currentPath);

        requestQuery = "<%= request.getParameter("q") %>";
        if (requestQuery != "null")
        {
            // search for all content by default. Take care to set an 50 offeset later.
            searchModel.setSearchString(requestQuery);
        }

        searchForm = Backbone.View.Factory.createView(document.querySelector("#searchFormContainer"), searchModel);
        Backbone.View.Factory.createView(document.querySelector("#searchResultsFormContainer"), searchModel);

        var initModalDialog = function ()
        {
            $("#modal-vod-from").modal({
                backdrop: true,
                keyboard: true,
                show: false
            });
            $("#modal-delete-confirmation").modal({
                backdrop: true,
                keyboard: true,
                show: false
            });
        }

        var showModal = function ()
        {
            $("#modal-vod-from").modal('show');

            $("#saveVodButton").unbind();
            $("#cancelVodButton").unbind();
            $("#saveVodButton").bind("click", saveVodButton_clickHandler);
            $("#cancelVodButton").bind("click", cancelVodButton_clickHandler);
        }

        var modal_hideHandler = function(event)
        {
            editForm.remove();
            $("#saveVodButton").unbind();
            $("#cancelVodButton").unbind();
        }

        var showDeleteConfirmationModal = function (contentPath)
        {
            $("#modal-delete-confirmation").modal('show');
            $("#deleteVodButton").unbind();
            $("#cancelDeleteVodButton").unbind();
            $("#deleteVodButton").bind("click",
                    function(event)
                    {
                        var delCmd = new DeleteVodCommand();
                        delCmd.execute(contentPath);
                        $("#modal-delete-confirmation").modal('hide');
                    });
            $("#cancelDeleteVodButton").bind("click",
                    function(event)
                    {
                        $("#modal-delete-confirmation").modal('hide');
                    });
        }


        var addNewButton_clickHandler = function (event)
        {
            editForm = Backbone.View.Factory.createView(document.querySelector("#editFormContainer"), new VodModel());
            showModal();
            $("#saveVodButton").html("Upload");
        }

        var cancelVodButton_clickHandler = function (event)
        {
            $("#modal-vod-from").modal('hide');
        }

        var saveVodButton_clickHandler = function (event)
        {
            if (editForm.submitForm())
            {
                cancelVodButton_clickHandler(null);
            }
        }

        var contentTable_clickHandler = function (event)
        {
            switch (event.target.name)
            {
            case "editContentButton":
                event.preventDefault();

                vodModel = new VodModel();
                vodModel.contentPath = event.target.getAttribute("data-content-path");
                editForm = Backbone.View.Factory.createView(document.querySelector("#editFormContainer"), vodModel);
                showModal();
                $("#saveVodButton").html("Save");
                break;
            case "deleteContentButton":
                event.preventDefault();
                showDeleteConfirmationModal(event.target.getAttribute("data-content-path"));
                break;
            }

        }

        var initUploadContainer = function()
        {
            Backbone.View.Factory.createView(
                    document.querySelector("#uploadContainer"),
                    ContentManagerContext.getInstance().getActiveUploads())
        }


        initModalDialog();
        initUploadContainer();

        $("#modal-vod-from").bind("hide", modal_hideHandler);
        $("#addNewButton").bind("click", addNewButton_clickHandler);
        $("#contentTable").bind("click", contentTable_clickHandler);

        window.onpopstate = function(event)
        {
            searchForm.handleUrlChange(event);
        }

        window.addEventListener("error", function (event, url, lineNumber)
        {
            if (! event.defaultPrevented)
                if (console != null)
                {
                    console.log(event.message);
                }
        });
    }


    $(window).load(init);
</script>


