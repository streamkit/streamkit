class window.VodModel extends AbstractModel
    ###
    The JCR path to the content.
     ###
    contentPath: null

    ###
    Reference of CreateVodCommand used to cancel upload
     ###
    _uploadCmd: null

    defaults:
        active: true
        progress : -1

    validation:
        title:
            required: true
            maxLength: 125
            minLength: 6
        created:
            required: true
        description:
            required: true
            minLength: 5
        author:
           required: true
           minLength: 4
        tags:
           required: true
        mediaFile:
           required: true
           msg: "Please select a video file"


    isValid: ->
       typeof @get('isValid') != "undefined" and @get('isValid') is true

    isNew: ->
      not @hasResourceType()

    hasResourceType: ->
       typeof @get('sling:resourceType') != "undefined" and @get('sling:resourceType') != null

    setResourceType: ->
       @set({'sling:resourceType' : 'mediacenter:vod'})

    setContent: ( content ) =>
        @set( content ) if content? && content["sling:resourceType"] == "mediacenter:vod"
        @validation.mediaFile.required = @isNew()

    # this is called by CreateVodCommand
    setUploadCmd: ( cmd ) -> @_uploadCmd = cmd
    getUploadCmd: -> @_uploadCmd

    cancelUpload: ( removeIfCancelled = true ) =>
        if ( @getUploadCmd() != null )
            cancelInProgress = @getUploadCmd().cancel()
            @getUploadCmd().removeFromActiveUploads() if not cancelInProgress and removeIfCancelled

