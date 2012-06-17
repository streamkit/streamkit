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

    getChannelPath: -> @get('channelPath')
    setChannelPath: (value) -> @set({channelPath:value})

