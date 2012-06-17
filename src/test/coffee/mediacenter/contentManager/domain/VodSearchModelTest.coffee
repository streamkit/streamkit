TestCase("VodSearchModelTest", {
    model: null,

    setUp: ->
        @model = new VodSearchModel()

    tearDown: ->
        @model.unbind()
        @model = null

    testSearchString: ->
        assertEquals("Default search string is wrong", "", @model.getSearchString() )
        eventCount = 0;
        @model.bind("change:searchString", -> eventCount++ )
        @model.setSearchString("aaa")
        assertEquals("aaa", @model.getSearchString() )
        @model.setSearchString("bbb")
        assertEquals(2, eventCount)
        @model.setSearchString(null)
        assertEquals("searchString should never be null", "", @model.getSearchString() )

    testRefreshSearchString: ->
        eventCount = 0;
        @model.bind("change:searchString", -> eventCount++ )
        @model.setSearchString("aaa")
        assertEquals(1, eventCount)
        @model.refreshSearchString()
        assertEquals(2, eventCount)

    testSearchResults: ->
        assertNull("Default search results should be null", @model.getSearchResults() )
        results = ["a"]
        @model.setSearchResults(results)
        assertSame(results, @model.getSearchResults() )

    testChannelPath: ->
        assertNull("Channel path should be null initially", @model.getChannelPath() )
        path = "content/channel/demo"
        @model.setChannelPath( path )
        assertEquals(path, @model.getChannelPath() )
})