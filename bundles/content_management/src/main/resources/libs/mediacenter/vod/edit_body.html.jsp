<%@page session="false" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%-- Ensure the presence of the Sling objects --%>
<sling:defineObjects/>

<script id="vodFormTemplate" type="text/template">

        <div id="messagesContainer" class="span9">
            <!-- <progress id="progressBar" min="0" max="100" value="0">0% complete</progress> -->

            <!-- container for confirmation messages -->
            <div id="messageBox" class="alert" style="display: none;"></div>
        </div>

        <form method="POST" id="vodForm" enctype="multipart/form-data" action="#" class="form-horizontal">
            <fieldset>
                <%--<legend>Vod Content</legend>--%>

                <div class="control-group">
                    <label class="control-label" for="mediaFile">
                        Media file:<sup title="This field is mandatory.">*</sup>
                    </label>
                    <div class="controls">
                        <input type="file" name="mediaFile" id="mediaFile"
                               accept="video/mp4" class="input-file"
                               data-value="model.mediaFile" />
                        <span class="help-inline"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="title">Title:<sup title="This field is mandatory.">*</sup></label>
                    <div class="controls">
                        <input class="input-xlarge" type="text" name="title" id="title"
                               data-value="model.title"
                               required="true"/>
                        <!-- size="30" aria-required="true" pattern="[a-zA-Z_0-9 -]{4,}" -->
                        <span class="help-inline"></span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="description">Description:<sup title="This field is mandatory.">*</sup></label>
                    <div class="controls">
                       <textarea class="input-xlarge" rows="3" cols="30" id="description" name="description"
                                 data-value="model.description"
                                 required="true"></textarea>
                        <span class="help-inline"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="author">Author:<sup title="This field is mandatory.">*</sup></label>
                    <div class="controls">
                        <input class="input-xlarge" type="text" id="author" name="author"
                               required="true"
                               data-value="model.author" />
                        <span class="help-inline"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="active">Active:</label>
                    <div class="controls">
                        <input type="checkbox" name="active" id="active"
                                data-checked="model.active"/>
                        <span class="help-inline"></span>
                        <%--<input type="hidden" name="active@TypeHint" value="Boolean" />--%>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="active">Tags:</label>
                    <div class="controls">
                        <input class="input-xlarge" type="text" name="tags" id="tags"
                               data-value="model.tags"
                               required="true" />
                        <span class="help-inline"></span>
                    </div>
                </div>

                 <div class="control-group">
                    <label class="control-label" for="active">Album:</label>
                     <div class="controls">
                        <input class="input-xlarge" type="text" name="album" id="album"
                               data-value="model.album" />
                         <span class="help-inline"></span>
                     </div>
                 </div>

                <div class="actions" style="display:none">
                    <button id="submitBtn" type="submit" style="display:none" class="btn primary">Process</button>
                </div>

            </fieldset>

        </form>

</script>

<%-- This following DIV is used only when this page is loaded through edit.html.jsp page --%>
<%-- When this page is loaded by a different page (i.e. list_body.html.jsp),
     then that page defines in which container this form appears --%>
<!--
<div id="editVodFormContainer" class="span12"
    data-component="EditVodForm"
    data-component-template="\${document.querySelector('#vodFormTemplate')}"></div>
-->