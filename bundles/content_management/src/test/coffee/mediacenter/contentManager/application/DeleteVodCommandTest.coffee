TestCase("DeleteVodCommandTest", {
    cmd : null
    server: null
    searchModel : null

    setUp: ->
        ContentManagerContext.getInstance().init()
        @cmd = new DeleteVodCommand()
        @server = sinon.fakeServer.create()
        @searchModel = ContentManagerContext.getInstance().getSearchModel()

    tearDown: ->
        @server.restore()
        @searchModel.unbind()

    testSuccessEnforcesSearch: ->
        url = "content/channel/demo/2012/1/vod"
        expectedResponse = """{"changes":[{"type":"deleted","argument":"#{url}"}],"referer":"http://localhost:8080/content/channel/demo.vodManager.html","path":"#{url}","location":"#{url}","parentLocation":"/channel/demo/vod/2012/1","status.code":200,"status.message":"OK","title":"Content modified /content/channel/demo/vod/2012/2/new video to delete"}"""
        fakeResponse = (xhr, id) ->
            xhr.respond( 200, { "Content-Type": "application/json" },
            expectedResponse )

        eventCount = 0;
        @searchModel.bind("change:searchString", -> eventCount++ )

        @server.respondWith("POST", url, fakeResponse )

        @cmd.execute(url)

        @server.respond()

        assertEquals("Search string should have been refreshed", 1, eventCount )

    testErrorHandler: ->
        # TODO: test that on errorhandler the Error is thrown
        assertTrue(true)
})