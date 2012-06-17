###
 # Used by UploadContainer in order to show info about a single upload.
 # When upload completes, it should show the success/error message.
 ###
class window.UploadControlBar extends Backbone.View
    model: null # new VodModel()
    template: null
    messageBox : null # MessageBox
    closeBtn : null

    constructor: ( obj ) ->
        {@template} = obj
        super(obj)

    initialize: ->
        console?.log("initializing UploadControlBar")
        @render()

    render: ->
        console?.log "rendering UploadControlBar"
        tmpl = _.template(@template.textContent)
        m = @model.toJSON()
        m.cid = @model.cid
        @el.innerHTML = tmpl(m)

        @renderMessageBox()

        @closeBtn = @el.querySelector(".closeButton")
        $(@closeBtn).bind("click", @closeBtn_clickHandler) if @closeBtn != null

        Backbone.ModelBinding.bind(this)

    renderMessageBox: =>
        @messageBox?.remove()
        msgBoxEl = @el.querySelector("[data-component='MessageBox']")
        @messageBox = Backbone.View.Factory.createView( msgBoxEl,@model ) if msgBoxEl

    closeBtn_clickHandler: =>
         @model.cancelUpload( true )

    remove: ->
        console?.log("removing ControlBar for Model #{@model.cid}")
        Backbone.ModelBinding.unbind(this)
        @messageBox?.remove()
        $(@closeBtn).unbind() if @closeBtn != null
        $(@el).remove()



