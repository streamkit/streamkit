class window.VideoSearchModel extends Backbone.Model

    defaults:
        searchString: ""
        searchResults: null
        channelPath:null

    getSearchString: -> @get('searchString')
    setSearchString: (value) ->
        @set({searchString:value || ""})
    refreshSearchString: -> @trigger("change:searchString")

    getSearchResults: -> @get('searchResults')
    setSearchResults: (value) -> @set({searchResults:value})

    getSearchContext: =>
        return @getAlbumPath() if @getAlbumPath() != ""
        return @getChannelPath()
    # setSearchContext: (value) -> @set({searchContext:value})

    getChannelPath: -> @get('channelPath')
    setChannelPath: (value) -> @set({channelPath:value})

    getAlbumPath: -> @get('albumPath')
    setAlbumPath: (value) -> @set({albumPath:value})

