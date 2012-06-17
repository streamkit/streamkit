class window.ContentManagerContext
    _instance = null

    searchModel = null

    activeUploads = null

    @getInstance : ->
        _instance ?= new ContentManagerContext()
        _instance

    init: ->
        searchModel = new VodSearchModel()
        activeUploads = new VodActiveUploadsModel()

    getSearchModel: -> searchModel
    getActiveUploads: -> activeUploads
