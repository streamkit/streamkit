###
    Builds search queries.
    When search string is empty or null, it creates a default query ordered by creation date
    else it creates a query ordered first by jcr:score and then by creation date
 ###
class window.SearchVodQueryBuilder

    @defaultProperties = ["title", "created", "active"]

    propertiesToInclude : null

    constructor: ( includeProperties = null ) ->
        @propertiesToInclude = includeProperties || SearchVodQueryBuilder.defaultProperties

    _getQueryByString: ( str, channelPath ) ->
        return channelPath + ".search.json?q=#{str}"

    _addProperties: ( queryString ) ->
        queryString += "&property=#{prop}" for prop in @propertiesToInclude when prop != null
        return queryString

    getQuery: ( searchString = "", channelPath = "" ) ->
        searchString = "" if searchString == null || typeof searchString == "undefined"
        return this._addProperties( this._getQueryByString( searchString, channelPath ) )