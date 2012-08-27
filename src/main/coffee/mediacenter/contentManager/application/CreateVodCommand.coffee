class window.CreateVodCommand extends AbstractSaveVodCommand
    _xhr: null
    _keepSessionAliveInterval : null
    _lastSessionPing : null

    _getActiveUploads: ->
        ContentManagerContext.getInstance().getActiveUploads()

    constructor: ( vodModel, keepAliveInterval = 300 * 1000 ) ->
        super(vodModel)
        @_keepSessionAliveInterval = keepAliveInterval
        vodModel.setUploadCmd( this )

    uploadStartedHandler: (event) =>
        console?.log ("Upload started #{@vodModel.get('progress')}")
        @vodModel.set({progress:0})
        @_getActiveUploads().add( @vodModel )
        @_lastSessionPing = new Date().getTime()

    uploadProgressHandler: (event) =>
        console?.log("Upload progress lengthComputable=#{event.lengthComputable}, event.loaded=#{event.loaded}, event.total= #{event.total}")
        prg = (event.loaded / event.total) * 100
        @vodModel.set({progress:prg})
        @_keepSessionAlive()

    _keepSessionAlive: ->
        now = new Date().getTime()
        return if ( now - @_lastSessionPing < @_keepSessionAliveInterval)
        @_pingServer()

    _pingServer: ->
        req = new XMLHttpRequest()
        req.open("GET","/system/sling/info.sessionInfo.json", true )
        req.send({":operation":"nop"})
        @_lastSessionPing = new Date().getTime()


    # After one of error, abort, or load has been
    uploadLoadedHandler: (event) =>
        if ( event != null &&  event.type == "abort" )
            @vodModel.addMessage("warning", "<p>Upload cancelled</p>")
            return

    uploadStateChangeHandler: (event) =>
        console?.log("Upload state change: #{event.currentTarget.readyState}")
        req = event.currentTarget
        if ( req.readyState == XMLHttpRequest.DONE )
            if ( req.status == 201 )
                @vodModel.addMessage("success", "<p>#{req.statusText}</p>")
                @vodModel.addMessage("warning", "<p>Your video is beeing processed and you're going to receive an email when processing is completed.</p>")
            else
                @vodModel.addMessage("error", "<p>#{req.statusText}</p>")

    execute: ( file ) ->
        @vodModel.resetMessages()

        # using XmlHttpRequest.send( formData ) method with progress event
        # spec: http://dvcs.w3.org/hg/xhr/raw-file/tip/Overview.html#the-send-method
        # spec: http://dvcs.w3.org/hg/xhr/raw-file/tip/Overview.html#event-xhr-progress
        # browser support: http://caniuse.com/#search=XmlHttpRequest
        @_xhr = new XMLHttpRequest()
        uploadData = new FormData()
        uploadData.append(key,value) for key,value of @createFormData()

        uploadData.append("mediaFile", file, file.name)

        @_xhr.addEventListener("loadstart", @uploadStartedHandler)
        @_xhr.addEventListener("progress", @uploadProgressHandler)
        @_xhr.addEventListener("loadend", @uploadLoadedHandler)
        @_xhr.addEventListener("readystatechange", @uploadStateChangeHandler)
        # TODO: add more handlers for error, timeout

        @_xhr.upload.onprogress = @uploadProgressHandler

        @_xhr.open('POST', @getActionUrl(), true)
        @_xhr.send( uploadData )
        return @_xhr

    cancel: ->
        console?.log "Cancelling upload, from state #{@_xhr.readyState}"
        if ( @_xhr.readyState !=  XMLHttpRequest.DONE)
            @_xhr.abort()
            return true
        return false

    removeFromActiveUploads: ->
        @_getActiveUploads().remove( @vodModel )