###
    Class showing multiple messages.
    Each message is an object like { type: "error", message: "messageText" }
 ###
class window.MessageBox extends Backbone.View
    ###
        The template used to display each message.
        Each message info is an object like { type: "error", message: "messageText" }.
        Note that the type of the message becomes the class of the messageTemplate too.

     ###
    _messageTemplate: """<li class="alert-message {{type}}"> {{message}} </li>"""
    model : AbstractModel

    constructor: ( obj ) ->
        @_messageTemplate = obj.messageTemplate if obj.messageTemplate
        super(obj)

    initialize: ->
        @watchModel()
        @render()

    render: ->
        console?.log "Rendering MessageBox"

    watchModel: ->
        @model.getMessages().bind("add", @renderMessages )
        @model.getMessages().bind("reset", @renderMessages )

    createMessage: (msg) =>
         _.template(@_messageTemplate, msg.toJSON())

    renderMessages: =>
        l = @model.getMessages().length
        if ( l == 0 )
            console?.log("No messages to show for model #{@model.cid}")
            $(@el).hide()
            $(@el).empty()
            return null

        messages = (@createMessage(msg) for msg in @model.getMessages().toArray()).join('')
        $(@el).html( messages )
        $(@el).show()

    remove: =>
        console?.log("removing MessageBox for Model #{@model.cid}")
        @model.getMessages().unbind("add", @renderMessages)
        @model.getMessages().unbind("reset", @renderMessages )
        @model = null
        $(@el).hide()
        $(@el).remove()


