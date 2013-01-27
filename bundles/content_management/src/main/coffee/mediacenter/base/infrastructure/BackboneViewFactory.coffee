###
    This container is responsible to show the active uploads
    Creates a Backbone View instance based on the data values of the given elementContainer.
    Example:
        <div id="myDiv"
             data-component="MyBackboneViewClass"
             data-component-template="${document.querySelector('myTmpl')}"
             data-component-item-renderer-template="${document.querySelector('itemRendererTmpl')}"
             data-component-my-other-prop="hello" >

        The above declaration will initialize MyBackboneViewClass with the arguments
        {
         template: document.querySelector('myTmpl'),
         itemRendererTemplate: document.querySelector('itemRendererTmpl'),
         myOtherProp: "hello"
        }

        You can pass any arbitrary arguments to the constructor.
        If the argument needs to be evaluated before being passed, declare it with ${statement}.

        In cases when ${statement} isn't fit for your context, you can change the template setting
        for statements, by changing:
            Factory.templateSettings = /\{\{(.+?)\}\}/g
            This example shows you how to change template settings to match with Mustache-like templates.
 ###
class Backbone.View.Factory
    @templateSettings: /\$\{(.+?)\}/gi

    getAttributeValue = ( el, attrName, attrValue ) ->
        if ( ! attrName || !attrValue )
            return null
        finalVal = {}
        finalVal[attrName] = attrValue
        regEx = Factory.templateSettings
        expr = attrValue.match(regEx)
        if ( expr != null )
            finalVal[attrName] = eval( attrValue.replace( regEx, "$1" ) )

        return finalVal

    ###
     Replaces letter coming after "-" with capital letter, removing the "-".
     For example: "item-renderer-template" is transformed into "itemRendererTemplate"
     ###
    getAttributeName = ( attrName ) ->
        regEx = /[-]([a-z])/gi
        str = attrName.replace( regEx, ($0,$1) -> $1.toUpperCase() )
        return str


    @createView: ( el, model ) ->
        viewClass = el.getAttribute("data-component")
        viewConstructorArgs = {
            el : el,
            model : model
        }
        dataAttributes = ( getAttributeValue( el, getAttributeName(attr.name[15..]), el.getAttribute(attr.name) ) for attr in el.attributes when attr.name.indexOf("data-component-") == 0 )
        ( viewConstructorArgs[key] = value ) for key,value of attribute for attribute in dataAttributes

        view = new window[viewClass]( viewConstructorArgs )
        return view

