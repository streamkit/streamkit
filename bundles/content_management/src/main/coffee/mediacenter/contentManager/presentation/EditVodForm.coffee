class window.EditVodForm extends Backbone.View
    model : null #new VodModel()
    _viewBinding : null # Rivets.View instance
    template : null
    lastValidationError: ""

    mb = 1024 * 1024;
    maxFileSizeMB = 1536

    constructor: (obj) ->
        {@template} = obj
        super(obj)

    initialize: ->
        console?.log "initializing EditVodForm"
        @template = @template.textContent
        @loadContentInfo()
        @watchModel()
        Backbone.Validation.bind(this, {
                valid: @modelValidHandler
                invalid: @modelInvalidHandler
            })
        @render()


    loadContentInfo: =>
        if ( not @model.contentPath )
            return
        currentNode = Sling.getContent( @model.contentPath, 0 )
        @model.setContent( currentNode )

    watchModel: =>
        @model.getMessages().bind("add", @messagesChangedHandler)
        @model.getMessages().bind("reset", @messagesChangedHandler )
        @model.bind("change:progress", @progressChangedHandler)

    render: =>
        console?.log "rendering EditVodForm"
        tmpl = _.template(@template)
        @el.innerHTML = tmpl()
        @setupBinding()
        if ( !@model.isNew() )
            $(@el).find("#mediaFile").parent().parent().hide()

        # if album is visible, initialize AlbumManagerVodForm
        # AlbumManagerVodForm can show, edit albums for the current VOD

        # if tags is visible, initialize TagManagerVodForm
        # TagManagerVodForm can show, edit tags for the current VOD

    setupBinding: =>
        console?.log "setting up Binding for EditVodForm"
#        Backbone.ModelBinding.bind(this);
        @_viewBinding = rivets.bind($(@el), {model: @model});

    events:
        "submit" : "form_submitHandler"

    submitForm: ->
        console?.log "submitting form from context #{this}"
        vodForm = document.getElementById("vodForm")
        # trigger form validation + submit
        vodForm.submitBtn.click()
        return @model.isValid()

    form_submitHandler: (event) ->
        console?.log "Saving content"
        event.preventDefault()

        @clearMessage()

        @model.validate()

        if ( not @model.isValid() )
            @showMessage("error","Form is invalid. #{@lastValidationError}")
            return false

        if ( @model.isNew() )
            if (document.querySelector('#vodForm input[type="file"]').files.length == 0 )
                @showMessage("error", "Please select a video file")
                return false
            file = document.querySelector('#vodForm input[type="file"]').files[0]
            if ( file && file.fileSize > (@maxFileSizeMB * @mb))
                @showMessage("error","File is too big. Maximum file size is #{@maxFileSizeMB} MB")
                return false

            cmd = new CreateVodCommand(@model)
            cmd.execute(file)
        else
            cmd = new UpdateVodCommand(@model)
            cmd.execute()
        return false

    showMessage: ( msgLevel, msgText) ->
        msgBox = $("#messageBox")
        msgBox.addClass(msgLevel)
        msgBox.html( msgText )
        msgBox.show()

    clearMessage: ->
        box = $("#messageBox")
        box.removeClass("success")
        box.removeClass("warning")
        box.removeClass("info")
        box.removeClass("error")
        box.html("")
        box.hide()

    messagesChangedHandler: =>
        m = @model.getMessages()
        if ( m != null && m.length > 0 )
            # at the moment only the first message is shown
           @showMessage( m.at(0).get("type"), m.at(0).get("message") )
        else
           @clearMessage()

    progressChangedHandler: =>
        progressBar = document.querySelector("#progressBar")
        if (progressBar)
          progressBar.value = @model.get("progress")
          progressBar.textContent = progressBar.value # Fallback for unsupported browsers.

    remove: ->
#        Backbone.ModelBinding.unbind(this)
        @_viewBinding.unbind()
        Backbone.Validation.unbind(this)
        $(@el).unbind()
        @model.getMessages().unbind("add", @messagesChangedHandler)
        @model.getMessages().unbind("reset", @messagesChangedHandler )
        @model.unbind("change:progress", @progressChangedHandler)
        @model = null
        @template = null
#        $(@el).remove()

    modelValidHandler: (view, attr) =>
        return if attr.indexOf(":") > 0
        viewEl = view.$('#' + attr)
        viewEl.removeClass('invalid')
        viewEl.removeAttr('data-error')

        viewElParent = viewEl.parent()?.parent()
        if ( viewElParent != null )
            viewElParent.removeClass("error")
            viewElParent.addClass("success")

        helpInlineEl = viewElParent.find(".help-inline")
        if ( helpInlineEl != null )
            helpInlineEl.html("")

        inputEl = viewEl[0]
        if ( !inputEl )
            return null
        inputEl.setCustomValidity?('')
        inputEl.title = ''
        view.lastValidationError = ""
        @lastValidationError = ""
        return null

    modelInvalidHandler: (view, attr, error) =>
        return if attr.indexOf(":") > 0
        viewEl = view.$('#' + attr)
        viewEl.addClass('invalid')
        viewEl.attr('data-error', error)

        viewElParent = viewEl.parent()?.parent()
        if ( viewElParent )
            viewElParent.removeClass("success")
            viewElParent.addClass("error")

        helpInlineEl = viewElParent.find(".help-inline")
        if ( helpInlineEl != null )
            helpInlineEl.html( error )

        htmlEl = viewEl[0]
        if ( !htmlEl )
            return null
        htmlEl.setCustomValidity?(error)
        htmlEl.title = error
        view.lastValidationError = error
        @lastValidationError = error
        return null