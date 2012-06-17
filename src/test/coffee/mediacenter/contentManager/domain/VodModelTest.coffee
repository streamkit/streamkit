TestCase("VodModelTest", {
    model: new VodModel()
    setUp: ->
        @model = new VodModel()

        Backbone.Validation.bind( {model:@model},
            {
                valid: (view, attr) ->
                    return null
                invalid: (view, attr, error) ->
                    return null
            })
    testHasResourceType: ->
        @model = new VodModel()
        assertFalse(@model.hasResourceType())
        @model.setResourceType()
        assertTrue(@model.hasResourceType())

    testIsNew: ->
        @model = new VodModel()
        assertTrue("model should have been new, but was:#{@model.isNew()}", @model.isNew())
        @model.setResourceType()
        assertFalse( @model.isNew() )

    testModelIsValid: ->
        @model = new VodModel()

    testAddingMessage: ->
        @model = new VodModel()
        @model.resetMessages()
        assertNotNull( @model.getMessages() )
        assertEquals(0, @model.getMessages().length )

        callbackCounter = 0
        @model.getMessages().bind("add", -> callbackCounter+=1)

        @model.addMessage("info", "info msg")
        @model.addMessage("error", "error msg")
        assertEquals( 2, @model.getMessages().length )
        assertEquals( "info", @model.getMessages().at(0).get("type") )
        assertEquals( "info msg", @model.getMessages().at(0).get("message") )
        assertEquals( 2, callbackCounter )

    testCancelUploadCommand: ->
        @model = new VodModel()
        cmd = new CreateVodCommand(@model)
        @model.setUploadCmd( cmd )
        assertSame( @model.getUploadCmd(), cmd)

        cancelUploadMethod = cmd.cancel
        uploadCancelled = false
        removedFromActiveUploadsQueue = false
        cmd.cancel = ->
            uploadCancelled = true
            return true

        cmd.removeFromActiveUploads = ->
            removedFromActiveUploadsQueue = true

        assertFalse( uploadCancelled )
        assertFalse( removedFromActiveUploadsQueue )

        @model.cancelUpload( true )
        assertTrue( uploadCancelled )
        assertFalse( removedFromActiveUploadsQueue )

        uploadCancelled = false
        cmd.cancel = ->
            uploadCancelled = true
            return false

        @model.cancelUpload( true )
        assertTrue( uploadCancelled )
        assertTrue( removedFromActiveUploadsQueue )

        cmd.cancel = cancelUploadMethod

})