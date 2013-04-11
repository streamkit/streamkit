TestCase("UploadControlBarTest",{
    bar: null
    vodModel : null
    template: null

    setUp: ->
        @vodModel = new VodModel({title:"video", progress: 25})
        templateText = """
                <div id="uploadTitle" class="uploadName" data-text="model.title"></div>
                <ul data-component="MessageBox"/>
                <progress id="uploadProgressBar" class="uploadProgressBar"
                            data-value="model.progress"
                            data-text="model.progress"
                            min="0" max="100" value="0"
                            >0% complete</progress>
                    """
        @template = {textContent: templateText}
        @bar = new UploadControlBar( {
                                tagName: 'li',
                                className: 'uploadRenderer',
                                id : @vodModel.cid,
                                model: @vodModel,
                                template: @template
                                            })

    testInitialization: ->
        titleEl = @bar.el.querySelector("#uploadTitle")
        progressEl = @bar.el.querySelector("#uploadProgressBar")
        messageBox = @bar.messageBox
        assertNotNull( titleEl )
        assertNotNull( progressEl )
        assertNotNull( messageBox )
        assertEquals( @vodModel.get("progress"), progressEl.value )
        assertEquals( @vodModel.get("title"), titleEl.innerHTML )

    testProgressUpdates: ->
        progressEl = @bar.el.querySelector("#uploadProgressBar")
        assertNotNull( progressEl )
        @vodModel.set({progress:33})
        assertEquals( 33, progressEl.value )
        @vodModel.set({progress:99})
        assertEquals( 99, progressEl.value )

    testMessagesGetDisplayed: ->
        @vodModel.addMessage( "success", "success_msg" )
        messageBox = @bar.messageBox
        assertNotNull( messageBox )
        assertEquals( """<li class="alert-message success"> success_msg </li>""" , messageBox.el.innerHTML )
        @vodModel.resetMessages()
        assertEquals( "" , messageBox.el.innerHTML )

    testCloseBtnClickHandler: ->
        cancelUploadMethod = @vodModel.cancelUpload
        uploadCancelled = false
        @vodModel.cancelUpload = -> uploadCancelled = true

        assertFalse ( uploadCancelled )
        @bar.closeBtn_clickHandler()
        assertTrue( uploadCancelled )

        @vodModel.cancelUpload = cancelUploadMethod

})