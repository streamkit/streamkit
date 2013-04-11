TestCase("UpdateVodCommandTest", {
    cmd: null
    model: null
    server : null

    setUp: ->
        @model = new VodModel()
        @cmd = new UpdateVodCommand( @model )
        @server = sinon.fakeServer.create()

    tearDown: ->
        @server.restore()

    testUpdateSuccess: ->
        @model.set({title:"vod_Test_ok"})
        @model.set({description:"some desc"})
        @model.set({'sling:resourceType':'mediacenter:vod'})
        @model.contentPath = "content/channel/demo/vod/2012/1/testVod"

        url = @cmd.getActionUrl()

        fakeResponse = (xhr, id) ->
            xhr.respond( 200, { "Content-Type": "application/json" },
            '{ "title": "vod_Test_ok", "description": "some desc" }' )

        @server.respondWith("POST", url, fakeResponse )

        @cmd.execute()
        assertEquals( 0, @model.getMessages().length )

        @server.respond()
        assertEquals( 1, @model.getMessages().length )
        assertEquals("success", @model.getMessages().at(0).get("type") )

    testUpdateError: ->
        @model.set({title:"vod_Test_ok"})
        @model.set({description:"some desc"})
        @model.set({'sling:resourceType':'mediacenter:vod'})
        @model.contentPath = "content/channel/demo/vod/2012/1/testVod"

        url = @cmd.getActionUrl()

        fakeResponse = (xhr, id) ->
            xhr.respond( 503, { "Content-Type": "application/json" },
                    '{ "error": { "class": "err_cls", "message":"err_msg"} }' )
        @server.respondWith("POST", url, fakeResponse )

        @cmd.execute()
        assertEquals( 0, @model.getMessages().length )

        @server.respond()
        assertEquals( 1, @model.getMessages().length )
        assertEquals("error", @model.getMessages().at(0).get("type") )

})