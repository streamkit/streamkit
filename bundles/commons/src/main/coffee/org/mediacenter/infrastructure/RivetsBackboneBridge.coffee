 ###
 Utility to configure rivets binding library for Backbone binding
  ###
 rivets?.configure({
          adapter: {
            subscribe: (obj, keypath, callback) ->
              obj.bind('change:' + keypath, callback)
            ,
            unsubscribe: (obj, keypath, callback) ->
              obj.unbind('change:' + keypath, callback)
            ,
            read: (obj, keypath) ->
              return obj.get(keypath)
            ,
            publish:(obj, keypath, value) ->
              obj.set(keypath, value)
          }
        })
