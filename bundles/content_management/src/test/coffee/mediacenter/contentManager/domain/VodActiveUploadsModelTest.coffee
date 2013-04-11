TestCase("VodActiveUpoadsModelTest",{
    model : null

    setUp: ->
        @model = new VodActiveUploadsModel()


    testVodModelAdded: ->
        m = new VodModel()
        counter = 0
        @model.bind("add", -> counter++)
        @model.add(m)
        assertEquals( 1, counter )
        assertEquals( 1, @model.length )

    testVodModelRemoved: ->
        m = new VodModel()
        counter = 0
        @model.bind("remove", -> counter++)
        @model.add(m)
        @model.remove(m)
        assertEquals( 1, counter )
        assertEquals( 0, @model.length )

    testReset: ->
        counter = 0
        @model.bind("reset", -> counter++)
        @model.add( new VodModel() )
        @model.add( new VodModel() )
        assertEquals( 2, @model.length )
        @model.reset()
        assertEquals( 1, counter )
        assertEquals( 0, @model.length )
})