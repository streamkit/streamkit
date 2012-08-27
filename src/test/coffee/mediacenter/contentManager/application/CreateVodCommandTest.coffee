AsyncTestCase("CreateVodCommandTest", {
    cmd: null
    model: null
    server : null

    setUp: ->
        @model = new VodModel()
        @cmd = new CreateVodCommand( @model )
        ContentManagerContext.getInstance().init()
        @server = sinon.fakeServer.create()

    tearDown: ->
        @server.restore()

    getActiveUploads: ->
        ContentManagerContext.getInstance().getActiveUploads()

    testInitialization: ->
        assertSame( @model.getUploadCmd(), @cmd )

    testUploadProgressAtStart: ->
        @cmd.uploadStartedHandler( null )
        assertEquals(0, @model.get("progress") )

    testSessionIsAliveDuringUpload: ( queue ) ->
        timeoutCount = 0

        @cmd = new CreateVodCommand( @model, 1000 )
        assertNull("Initially there should be no ping to keep session alive",  @cmd._lastSessionPing )

        startTime = new Date()
        pingTime = null

        fakeResponse = (xhr, id) ->
            console?.log "sending fake ping response to the client"
            xhr.respond( 200, { "Content-Type": "application/json" }, '[{ "Message": "Null Operation Status: 200" }]' )
        @server.respondWith("GET", "/system/sling/info.sessionInfo.json", fakeResponse )

        @cmd.uploadStartedHandler( null )
        assertNotNull("Last ping should be considered the command to start uploading", @cmd._lastSessionPing )
        assertTrue( @cmd._lastSessionPing - startTime.getTime() < 1000 )

        queue.call("Simulate progress", (callbacks) ->
            myCallback = callbacks.add( -> timeoutCount++ )
            @cmd.uploadProgressHandler({loaded:10, total:100})
            assertTrue( @cmd._lastSessionPing - startTime.getTime() < 1000 )
            setTimeout(myCallback, 1500) )

        queue.call("Verify session has been kept alive", ->
            now = new Date().getTime()
            @cmd.uploadProgressHandler({loaded:50, total:100})
            assertFalse("_lastSessionPing should have been updated.",  @cmd._lastSessionPing - startTime.getTime() < 1000 )
            assertTrue( @cmd._lastSessionPing - now < 1000 )
            )


    testUploadProgressHandler:->
        @cmd.uploadProgressHandler({loaded:50, total:100})
        assertEquals(50,  @model.get("progress") )

    testActiveUploadsIsUpdated: ->
        assertEquals( 0, @getActiveUploads().length )
        @cmd.uploadStartedHandler( null )
        assertEquals( 1, @getActiveUploads().length )
        @cmd.removeFromActiveUploads()
        assertEquals( 0, @getActiveUploads().length )

    testUploadCancelledHandler: ->
        @model.set({progress:29})
        @cmd.uploadLoadedHandler({type:"abort"})
        assertEquals("progress should not be changed", 29, @model.get("progress"))
        assertNotNull( "messages should be present", @model.getMessages() )
        assertEquals( "there should be at least 1 message",  @model.getMessages().length, 1 )
        assertEquals( "message type is wrong", @model.getMessages().at(0).get("type"), "warning" )



#    testExecute:->
#        t = new Date()
#        url = "content/channel/demo/vod/#{t.getFullYear()}/#{t.getMonth()+1}/vod_Test_ok"
#
#        fakeResponse = (xhr, id) ->
#            console?.log "sending fake response back to the client"
#            xhr.respond( 201, { "Content-Type": "application/json" }, '[{ "title": vod_Test, "description": "some desc" }]' )
#        @server.respondWith("POST", url, fakeResponse )
#
#        @model.set({title:"vod_Test_ok"})
#        @model.set({description:"some desc"})
#
#        window.Sling = {currentPath: "content/channel/demo"}
#
#        assertEquals( @cmd.getActionUrl(), url )
#
#        @cmd.execute( { name: "video_file.mp4"} )
#        assertEquals( @model.getMessages().length, 0 )
#
#        @server.respond()
#        assertEquals( @model.getMessages().length, 1 )

})