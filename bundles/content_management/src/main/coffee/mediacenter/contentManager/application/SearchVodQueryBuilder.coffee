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

    _getDefaultQuery: ( channelPath )->
        orderByStatement = "order by @jcr:created descending"
        return this._createQueryString( "", channelPath, orderByStatement )

    _getQueryByString: ( str, channelPath ) ->
        orderByStatement = "order by @jcr:score descending, @jcr:created descending"
        return this._createQueryString( str, channelPath, orderByStatement )

    _createQueryString: ( str, channelPath, orderByStatement ) ->
        xPathStatement="//(@title)[jcr:contains(.,'#{str}*'),sling:resourceType='mediacenter:vod']" #/(rep:excerpt(.))"
        queryString = channelPath + ".content-query.tidy.json?queryType=xpath&statement=#{xPathStatement}"
        queryString += orderByStatement

    _addProperties: ( queryString ) ->
        queryString += "&property=#{prop}" for prop in @propertiesToInclude when prop != null
        return queryString

    getQuery: ( searchString = "", channelPath = "" ) ->
        searchString = "" if searchString == null || typeof searchString == "undefined"
        if ( searchString.length < 1)
            return this._addProperties( this._getDefaultQuery( channelPath ) )
        return this._addProperties( this._getQueryByString( searchString, channelPath ) )