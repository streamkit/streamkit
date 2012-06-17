TestCase("AbstractModelTest", {
    model: null

    testInitialization: ->
        @model = new AbstractModel()
        assertNotNull( @model.getMessages() )
        assertEquals(0, @model.getMessages().length )

    testAddingMessage: ->
        @model = new AbstractModel()
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
        jsonEl = @model.getMessages().at(0).toJSON()
        assertEquals( "info", jsonEl.type )
        assertEquals( "info msg", jsonEl.message )
        assertEquals( 2, callbackCounter )

    testResetMessages: ->
        @model = new AbstractModel()

        callbackCounter = 0
        @model.getMessages().bind("reset", -> callbackCounter+=1)
        @model.addMessage("info", "info msg")
        @model.addMessage("error", "error msg")

        @model.resetMessages()

        assertEquals( 1, callbackCounter )
        assertNotNull( @model.getMessages() )
        assertEquals(0, @model.getMessages().length )
})