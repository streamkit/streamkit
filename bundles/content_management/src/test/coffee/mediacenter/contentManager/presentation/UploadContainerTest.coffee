TestCase("UploadContainerTest",{
    container: null

    getActiveUploads: ->
        ContentManagerContext.getInstance().getActiveUploads()

    setUp: ->
        _.templateSettings = {
            interpolate : /{{(.+?)}}/g
        }
        ContentManagerContext.getInstance().init()
        @container = new UploadContainer({
                            template: {textContent: "<ul></ul>"},
                            itemRendererTemplate: {textContent: """<li class="uploadRenderer"><div>{{title}}</div></li>"""}
                                })

    testInitialization: ->
        assertEquals("<ul></ul>", @container.el.innerHTML )

    testNewUploadGetsDisplayed: ->
        assertEquals(0, @container.el.querySelector("ul").childNodes.length )
        newVideo = new VodModel({ title: "new video" })
        @getActiveUploads().add( newVideo )
        assertEquals(1, @container.el.querySelector("ul").childNodes.length )
        liEl = @container.el.querySelector("#uploadCtrlBar_#{newVideo.cid}")
        assertNotNull( liEl )
        assertEquals("uploadRenderer", liEl.getAttribute("class"))
        #assertEquals("<ul><li class=\"uploadRenderer\" id=\"#{newVideo.cid}\"><div>new video</div></li></ul>", @container.el.innerHTML )

    testUploadGetsRemoved: ->
        newVideo1 = new VodModel({ title: "video1" })
        @getActiveUploads().add( newVideo1 )
        newVideo2 = new VodModel({ title: "new video2" })
        @getActiveUploads().add( newVideo2 )
        assertEquals(2, @container.el.querySelector("ul").childNodes.length )

        @getActiveUploads().remove( newVideo1 )
        assertEquals(1, @container.el.querySelector("ul").childNodes.length )
        liEl = @container.el.querySelector("#uploadCtrlBar_#{newVideo2.cid}")
        assertNotNull( liEl )
        assertEquals("uploadRenderer", liEl.getAttribute("class"))
        #assertEquals("<ul><li class=\"uploadRenderer\" id=\"#{newVideo2.cid}\"><div>new video2</div></li></ul>", @container.el.innerHTML )

    testReset: ->
        newVideo1 = new VodModel({ title: "video1" })
        @getActiveUploads().add( newVideo1 )
        newVideo2 = new VodModel({ title: "new video2" })
        @getActiveUploads().add( newVideo2 )
        assertEquals(2, @container.el.querySelector("ul").childNodes.length )

        @getActiveUploads().reset()
        assertEquals(0, @container.el.querySelector("ul").childNodes.length )
        assertEquals("<ul></ul>", @container.el.innerHTML)
})