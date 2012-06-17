###
    This container is responsible to show the active uploads
 ###
class window.UploadContainer extends Backbone.View
    template: null
    itemRendererTemplate: null
    _renderers: {}

    constructor: (obj) ->
        {@template, @itemRendererTemplate} = obj
        super(obj)

    getActiveUploads = ->
        ContentManagerContext.getInstance().getActiveUploads()

    resetRenderers = ->
        # removeRenderer( renderers[key] ) for key,value of renderers
        @_renderers = {}

    initialize: ->
        console?.log "Initializing UploadContainer"
        @watchActiveUploads()
        @render()

    render: ->
        console?.log "rendering UploadContainer"
        tmpl = _.template(@template.textContent)
        @el.innerHTML = tmpl()
#        @setupBinding()

    watchActiveUploads:->
        getActiveUploads().bind("add", @uploadAddedHandler )
        getActiveUploads().bind("remove", @uploadRemovedHandler )
        getActiveUploads().bind("reset", @uploadResetHandler )

    uploadAddedHandler: (vodModel ) =>
        @_renderers[vodModel.cid] = new UploadControlBar( {
                                            tagName: 'li',
                                            className: 'uploadRenderer',
                                            id : "uploadCtrlBar_#{vodModel.cid}",
                                            model: vodModel,
                                            template: @itemRendererTemplate
                                                        })
        @el.querySelector("ul").appendChild(@_renderers[vodModel.cid].el)

    uploadRemovedHandler: ( vodModel ) =>
        ctrlBar = @_renderers[vodModel.cid]
        ctrlBar.remove()
        delete @_renderers[vodModel.cid]

    uploadResetHandler: =>
        @uploadRemovedHandler( value.model ) for key,value of @_renderers
        resetRenderers()
