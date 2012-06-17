class window.VideoResultsForm extends Backbone.View
    model :                     null # new VideoSearchModel()
    itemRendererTemplate:       null

    list:                       null

    listDataProvider :          null

    constructor: (obj) ->
        {@template, @itemRendererTemplate} = obj
        super(obj)

    initialize: ->
        console?.log "initializing VideoResultsForm"
        @render()
        @watchSearchModel()

    watchSearchModel: ->
        @model.bind("change:searchString", @doSearch)

    render: =>
        console?.log "rendering VideoResultsForm"
        @setUpInfiniteList()

        @doSearch() unless  @model.getSearchString() == "null"

    setUpInfiniteList: ->
        @listDataProvider = new iList.LazyDataProvider({
            source:         @model.getSearchResults()
            loadPolicy:     new iList.loadPolicy.WindowScroll(),  # loads more when browser window is scrolled down
            loader:         new iList.loader.AjaxDataLoader({ url:"", rows:15, offset:0}),
            dataConverter:  (data) ->
                return ({
                        element:   vodItem,
                        index:      _i+1,
                        path:       vodItem['jcr:path'],
                        created:    $.timeago( new Date(vodItem['jcr:created']))} for vodItem in data )

        })

        @list = new iList.InfiniteList({
            # VIEW configuration
            container:              $(@el),
            itemRendererTemplate:   @itemRendererTemplate,
            templateFunction:       _.template,
            loaderSelector:         "#libraryLoadingAnimation",
            # Domain
            dataProvider:           @listDataProvider  } )


    doSearch: (m, searchString) =>
        console?.log("searching for #{@model.getSearchString()}")

        # reset the results list and remove elements except the listLoader
        @listDataProvider.reset()
        $(@el).find("a:not(:last-child)").remove()

        queryBuilder = new SearchVodQueryBuilder(["title", "jcr:created", "active"])
        queryString = queryBuilder.getQuery( @model.getSearchString(), @model.getChannelPath() )

        @listDataProvider.getLoader().url = queryString
        @listDataProvider.getLoader().offset = 0

        @listDataProvider.loadMore()

    remove: ->
        @model.unbind("change:searchString", this.doSearch)
        @model.unbind("change:searchResults", this.renderResults)
