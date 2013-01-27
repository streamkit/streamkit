class window.VodSearchForm extends Backbone.View
    model : null # VodSearchModel

    constructor: (obj) ->
        super(obj)

    initialize: ->
        console?.log "initializing VodSearchForm"
        @render()

    updateText: (m, text) =>
        txt = @model.getSearchString()
        if txt == "null"
            txt = ""
        console?.log "updating text to:" + txt
        $("#searchInput").val( txt  )

    render: =>
        console?.log "rendering VodSearchForm "
        @model.bind("change:searchString", this.updateText)
        @updateText();
        return this

    events:
        "submit" : "form_submitHandler"

    form_submitHandler: ->
        console?.log "submit handler"
        # uncomment the next lines to perform the search without reloading the page
        @model.setSearchString( $("#searchInput").val() )
        history?.pushState(@model.getSearchString(), "Channel Manager", "?q=#{@model.getSearchString()}")
        return false

    getParam: ( exp, param) ->
        rgexp = ///
                (#{param}=(.*?)($|&))
                ///g
        exp2 = exp.match(rgexp)?.toString()
        exp2 = exp2?[param.length+1...].toString()
        exp2 = exp2?.replace("&","")
        if ( !exp2 )
            return null
        unescape(exp2)

    handleUrlChange: ( event ) =>
        loc = window.location.search
        v = @getParam( loc, "q")
        @model.setSearchString(v)
        console?.log "handleUrlChange : " + v

