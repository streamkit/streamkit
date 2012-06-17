TestCase("AbstractSaveVodCommandTest", {
    cmd: null
    model: null

    setUp: ->
        @model = new VodModel()
        @cmd = new AbstractSaveVodCommand( @model )
        @server = sinon.fakeServer.create()
        sinon.useFakeXMLHttpRequest()

    tearDown: ->
        @server.restore()

    testActionUrlForNewVOD: ->
        @model.set({title:"vod_Test_ok"})
        @model.set({description:"some desc"})
        window.Sling = {currentPath: "content/channel/demo"}
        t = new Date()
        assertEquals("actionUrl is wrong", @cmd.getActionUrl(), "content/channel/demo/vod/#{t.getFullYear()}/#{t.getMonth()+1}/vod_Test_ok" )

    testActionUrlForExisintgVOD: ->
        @model.set({title:"vod_Test_ok"})
        @model.set({description:"some desc"})
        @model.set({'sling:resourceType':'mediacenter:vod'})
        @model.contentPath = "content/channel/demo/vod/2012/1/testVod"
        assertEquals("actionUrl is wrong", @cmd.getActionUrl(), @model.contentPath )

    testVodHasCorrectResourceType: ->
        formData = @cmd.createFormData()
        assertEquals(formData['sling:resourceType'], 'mediacenter:vod')

    testRequestIsJSON: ->
        formData = @cmd.createFormData()
        assertEquals(formData[":http-equiv-accept"], "application/json,*/*;q=0.9")

    testRequestHasRightFields: ->
        @model.set({progress: 35})
        @model.set({isValid: true})
        @model.set({messages: [] })
        formData = @cmd.createFormData()
        assertUndefined( formData.messages )
        assertUndefined( formData.isValid )
        assertUndefined( formData.progress )

    testRequestHasCreateModifyInfo: ->
        formData = @cmd.createFormData()
        assertEquals(formData["jcr:created"], "")
        assertEquals(formData["jcr:createdBy"], "")
        assertEquals(formData["jcr:lastModified"], "")
        assertEquals(formData["jcr:lastModifiedBy"], "")


})