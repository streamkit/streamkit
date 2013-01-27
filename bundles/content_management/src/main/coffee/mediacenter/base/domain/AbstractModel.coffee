###
 # AbstractModel managing a collection of messages that should be displayed in the UI.
 # Messages could be manually set by calling addMessage( type,msg ) or could come from a service call,
 # or from a validation operation.
 # Messages should contain objects like { type: "info", message: "some message" }
 # Type could be : info, success, warning and error.
 #
 # Use this model when you need to display on the screen validation messages, or service calls results.
 ###
class window.AbstractModel extends Backbone.Model
    initialize: ->
        @set({messages: new Backbone.Collection})

    getMessages: ->
        @get("messages")

    resetMessages: ->
        @getMessages().reset()

    addMessage: ( type, msg ) ->
        @getMessages().add( [{type: type, message: msg}] )