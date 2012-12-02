class window.AbstractSaveVodCommand
    vodModel : null

    constructor: ( @vodModel ) -> @

    createFormData: ->
        #preparing request
        formData = @vodModel.toJSON()
        formData['sling:resourceType'] = 'mediacenter:vod'
        formData[':http-equiv-accept'] = 'application/json,*/*;q=0.9'
        formData['jcr:created'] = "" # Sling should auto-fill this value through SlingPropertyValueHandler.java
        formData['jcr:createdBy'] = "" # Sling should auto-fill this value
        formData['jcr:lastModified'] = "" # Sling should auto-fill this value
        formData['jcr:lastModifiedBy'] = "" # Sling should auto-fill this value
        formData['active@TypeHint'] = 'Boolean'

        # To conform with Apache Sling specs at
        # http://sling.apache.org/site/manipulating-content-the-slingpostservlet-servletspost.html
        # Chapter: Algorithm for Node Name Creation
        if ( @vodModel.isNew() )
            formData[':nameHint'] = formData.title

        delete formData.isValid
        delete formData.messages
        delete formData.progress
        delete formData.mediaFile
        return formData

    getActionUrl: ->
        actionUrl = @vodModel.contentPath
        today = new Date()
        if ( @vodModel.isNew() )
            actionUrl = "#{Sling.currentPath}/vod/#{today.getFullYear()}/#{(today.getMonth()+1)}/"
        return actionUrl
