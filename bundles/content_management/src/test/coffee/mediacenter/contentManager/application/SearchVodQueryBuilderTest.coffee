TestCase( "SearchVodQueryBuilder", {
    testDefaultProperties: ->
        builder = new SearchVodQueryBuilder()
        props = builder.propertiesToInclude
        assertNotNull ( props )
        assertTrue( props.length > 0 )

        hasTitle = false
        for prop in props
            hasTitle = true if prop == "title"
        assertTrue( "at lest 'title' should be included in the query", hasTitle )

    testConstructor: ->
        customProps =  ["title", "modified"]
        builder = new SearchVodQueryBuilder( customProps )
        props = builder.propertiesToInclude
        assertNotNull ( props )
        assertEquals( 2, props.length )
        assertSame( customProps, props )


    testQueryByString: ->
        builder = new SearchVodQueryBuilder(["title"])
        q = builder.getQuery( "123", "http://localhost:8080/content" )
        expectedQuery = this._createQueryString("123")
        expectedQuery += "order by @jcr:score descending, @jcr:created descending"
        expectedQuery += "&property=title"
        assertEquals( expectedQuery, q )

    testQueryWithEmptyString: ->
        builder = new SearchVodQueryBuilder(["title"])
        q = builder.getQuery( "", "http://localhost:8080/content" )
        expectedQuery = this._createQueryString("")
        expectedQuery += "order by @jcr:created descending"
        expectedQuery += "&property=title"
        assertEquals( expectedQuery, q )

    testQueryWithNoExtraProperties: ->
        builder = new SearchVodQueryBuilder([])
        q = builder.getQuery( "123", "http://localhost:8080/content" )
        expectedQuery = this._createQueryString("123")
        expectedQuery += "order by @jcr:score descending, @jcr:created descending"
        assertEquals( expectedQuery, q )


    _createQueryString: (str) ->
        expectedQuery = "http://localhost:8080/content.content-query.tidy.json?queryType=xpath&statement="
        expectedQuery += "//(@title)[jcr:contains(.,'#{str}*'),sling:resourceType='mediacenter:vod']"
        return expectedQuery
})