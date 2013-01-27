TestCase("MessageBoxTest", {
    box: null
    model: null

    setUp: ->
        _.templateSettings = {
            interpolate : /{{(.+?)}}/g
        }
        @model = new AbstractModel()
        @box = new MessageBox({
            model:@model,
            messageTemplate:"""<li class="alert-message {{type}}"> {{message}} </li>"""})

    testMessagesAreDisplayed: ->
        el = @box.el
        assertEquals( "", el.innerHTML )
        @model.addMessage("info", "infomsg" )
        assertEquals("""<li class="alert-message info"> infomsg </li>""", el.innerHTML )
        @model.addMessage("error", "errormsg" )
        assertEquals("""<li class="alert-message info"> infomsg </li><li class="alert-message error"> errormsg </li>""", el.innerHTML )

    testMessagesAreRemoved: ->
        el = @box.el
        assertEquals( "", el.innerHTML )
        @model.addMessage("info", "infomsg" )
        assertEquals("""<li class="alert-message info"> infomsg </li>""", el.innerHTML )
        @model.resetMessages()
        assertEquals("", el.innerHTML)
})