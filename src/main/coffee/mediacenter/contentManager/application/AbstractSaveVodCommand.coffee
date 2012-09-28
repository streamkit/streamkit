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
        delete formData.isValid
        delete formData.messages
        delete formData.progress
        return formData

    getActionUrl: ->
        actionUrl = @vodModel.contentPath
        today = new Date()
        titleText = @vodModel.get('title')
        if ( @vodModel.isNew() )
            actionUrl = "#{Sling.currentPath}/vod/#{today.getFullYear()}/#{(today.getMonth()+1)}/#{titleText}"
        return actionUrl
