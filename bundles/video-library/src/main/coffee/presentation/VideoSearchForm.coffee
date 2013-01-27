class window.VideoSearchForm extends Backbone.View
    model :                 null # VideoSearchModel
    searchInputSelector :   "#librarySearchInput"
    searchInput:            null

    constructor: (obj) ->
        {@searchInputSelector} = obj
        @searchInput = $(@searchInputSelector)
        super(obj)

    initialize: ->
        console?.log "initializing VideoSearchForm"
        @render()

    updateText: (m, text) =>
        txt = @model.getSearchString()
        if txt == "null"
            txt = ""
        @searchInput.val( txt  )

    listenForTextChanges: =>
        @searchInput.on("keyup", @seachInput_keyUpHandler )

    seachInput_keyUpHandler: (event) =>
        v = @searchInput.val()
        if ( v.length % 2 == 0 )
            @model.setSearchString( @searchInput.val() )

    render: =>
        console?.log "rendering VideoSearchForm "
        @model.bind("change:searchString", this.updateText)
        @updateText();
        @listenForTextChanges()
        return this

    events:
        "submit" : "form_submitHandler"

    form_submitHandler: ->
        console?.log "submit handler"
        @model.setSearchString( @searchInput.val() )
        return false

