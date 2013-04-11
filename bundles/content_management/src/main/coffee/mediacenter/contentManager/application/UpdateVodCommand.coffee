class window.UpdateVodCommand extends AbstractSaveVodCommand

    form_successHandler: (data, textStatus, xmlHttpRequest) ->
        action = "updated"
        action = "created" if @vodModel.isNew() is true
        @vodModel.addMessage("success", "<p>Content #{action} successfully!</p>")

    form_errorHandler: (xmlHttpRequest, textStatus, errorThrown) ->
        error = jQuery.parseJSON(xmlHttpRequest.responseText).error
        @vodModel.addMessage( "error", "<p>Error saving content.<br/> #{error.class}:#{error.message}</p>")


    execute: ->
        @vodModel.resetMessages()

        formData = @createFormData()
        delete formData.mediaFile

        $.ajax({
            url: @getActionUrl(),
            type: 'POST',
            data: formData,
            context: @,
            success: @form_successHandler,
            error: @form_errorHandler
        })