class window.DeleteVodCommand

    form_successHandler: (data, textStatus, xmlHttpRequest) ->
        searchModel = ContentManagerContext.getInstance().getSearchModel()
        searchModel.refreshSearchString()
        # TODO: discuss whether to display another dialog to confirm the result

    form_errorHandler: (xmlHttpRequest, textStatus, errorThrown) ->
        error = jQuery.parseJSON(xmlHttpRequest.responseText).error
        throw new Error("<p>Error deleting content.<br/> #{error.class}:#{error.message}</p>")

    execute: (vodContentPath) ->
        formData = {}
        formData[':http-equiv-accept'] = 'application/json,*/*;q=0.9'
        formData[':operation'] = 'delete'

        $.ajax({
            url: vodContentPath,
            type: 'POST',
            data: formData,
            context: @,
            success: @form_successHandler,
            error: @form_errorHandler
        })