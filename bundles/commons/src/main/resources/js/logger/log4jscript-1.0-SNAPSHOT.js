
/*
 Utility to create a particular namespace for Log4JScript
*/

(function() {

  window.namespace = function(_name) {
    var cob, createPackage, pkg, spc, _i, _len;
    createPackage = function(_parentPkg, _src) {
      return _parentPkg[_src] = _parentPkg[_src] || new Object();
    };
    cob = "";
    spc = _name.split(".");
    cob = window;
    for (_i = 0, _len = spc.length; _i < _len; _i++) {
      pkg = spc[_i];
      cob = createPackage(cob, pkg);
    }
    return null;
  };

}).call(this);

(function() {

  namespace("log.appender");

  log.appender.AbstractAppender = (function() {

    AbstractAppender.prototype.layout = null;

    function AbstractAppender(layout) {
      this.layout = layout;
    }

    AbstractAppender.prototype.doAppend = function(loggingEvent) {
      throw new Error("Please implement doAppend method in the supper class");
    };

    return AbstractAppender;

  })();

}).call(this);

(function() {
  var __hasProp = Object.prototype.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  namespace("log.appender");

  log.appender.AjaxAppender = (function(_super) {

    __extends(AjaxAppender, _super);

    AjaxAppender.prototype.endpoint = null;

    function AjaxAppender(layout, endpoint) {
      this.endpoint = endpoint;
      AjaxAppender.__super__.constructor.call(this, layout);
    }

    AjaxAppender.prototype.doAppend = function(loggingEvent) {
      var formData, msg, xhr;
      msg = this.layout.format(loggingEvent);
      xhr = this.getXHR();
      xhr.open('POST', this.endpoint, true);
      formData = new FormData();
      formData.append("formattedLogMessage", msg);
      formData.append("categoryName", loggingEvent.categoryName);
      formData.append("level", loggingEvent.level.toString());
      formData.append("message", loggingEvent.message);
      formData.append("exception", loggingEvent.exception);
      return request.send(formData);
    };

    AjaxAppender.prototype.getXHR = function() {
      var xhr;
      xhr = null;
      try {
        return xhr = new XMLHttpRequest();
      } catch (error) {
        return xhr = new ActiveXObject('Microsoft.XMLHTTP');
      }
    };

    return AjaxAppender;

  })(log.appender.AbstractAppender);

}).call(this);

(function() {
  var __hasProp = Object.prototype.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  namespace("log.appender");

  log.appender.ConsoleAppender = (function(_super) {

    __extends(ConsoleAppender, _super);

    function ConsoleAppender(layout) {
      ConsoleAppender.__super__.constructor.call(this, layout);
    }

    ConsoleAppender.prototype.doAppend = function(loggingEvent) {
      var msg;
      msg = this.layout.format(loggingEvent);
      switch (loggingEvent.level.toString()) {
        case "INFO":
          return typeof console !== "undefined" && console !== null ? console.info(msg) : void 0;
        case "WARN":
          return typeof console !== "undefined" && console !== null ? console.warn(msg) : void 0;
        case "ERROR":
        case "FATAL":
          return typeof console !== "undefined" && console !== null ? console.error(msg, loggingEvent.exception) : void 0;
        default:
          return typeof console !== "undefined" && console !== null ? console.log(msg) : void 0;
      }
    };

    return ConsoleAppender;

  })(log.appender.AbstractAppender);

}).call(this);

(function() {

  namespace("log.impl");

  log.impl.Logger = (function() {

    Logger.prototype.name = null;

    Logger.prototype.level = null;

    Logger.prototype.appenders = [];

    function Logger(name) {
      this.name = name;
    }

    Logger.prototype.setLevel = function(level) {
      this.level = level;
    };

    Logger.prototype.addAppender = function(appender) {
      if (!this.appenderExists(appender)) return this.appenders.push(appender);
    };

    Logger.prototype.appenderExists = function(appender) {
      var a, _i, _len, _ref;
      _ref = this.appenders;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        a = _ref[_i];
        if (appender === a) return true;
      }
      return false;
    };

    Logger.prototype.setAppenders = function(appendersList) {
      return this.appenders = appendersList;
    };

    Logger.prototype.isTraceEnabled = function() {
      if (this.level.valueOf() <= log.Level.TRACE.valueOf()) return true;
      return false;
    };

    Logger.prototype.isDebugEnabled = function() {
      if (this.level.valueOf() <= log.Level.DEBUG.valueOf()) return true;
      return false;
    };

    Logger.prototype.isInfoEnabled = function() {
      if (this.level.valueOf() <= log.Level.INFO.valueOf()) return true;
      return false;
    };

    Logger.prototype.isWarnEnabled = function() {
      if (this.level.valueOf() <= log.Level.WARN.valueOf()) return true;
      return false;
    };

    Logger.prototype.isErrorEnabled = function() {
      if (this.level.valueOf() <= log.Level.ERROR.valueOf()) return true;
      return false;
    };

    Logger.prototype.isFatalEnabled = function() {
      if (this.level.valueOf() <= log.Level.FATAL.valueOf()) return true;
      return false;
    };

    Logger.prototype.log = function(level, msg, exception) {
      var appender, _i, _len, _ref, _results;
      _ref = this.appenders;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        appender = _ref[_i];
        _results.push(appender.doAppend(new log.impl.LogginEvent(this.name, level, msg, exception, this)));
      }
      return _results;
    };

    Logger.prototype.trace = function(msg) {
      if (this.isTraceEnabled()) return log(log.Level.TRACE, msg, null);
    };

    Logger.prototype.debug = function(msg) {
      if (this.isDebugEnabled()) return log(log.Level.DEBUG, msg, null);
    };

    Logger.prototype.info = function(msg) {
      if (this.isInfoEnabled()) return log(log.Level.INFO, msg, null);
    };

    Logger.prototype.warn = function(msg, exception) {
      if (this.isWarnEnabled()) return log(log.Level.WARN, msg, exception);
    };

    Logger.prototype.error = function(msg, exception) {
      if (this.isErrorEnabled()) return log(log.Level.ERROR, msg, exception);
    };

    Logger.prototype.fatal = function(msg, exception) {
      if (this.isFatalEnabled()) return log(log.Level.FATAL, msg, exception);
    };

    return Logger;

  })();

}).call(this);

(function() {

  namespace("log.impl");

  log.impl.LoggingEvent = (function() {

    LoggingEvent.prototype.categoryName = null;

    LoggingEvent.prototype.level = null;

    LoggingEvent.prototype.message = null;

    LoggingEvent.prototype.exception = null;

    LoggingEvent.prototype.logger = null;

    function LoggingEvent(categoryName, level, message, exception, logger) {
      this.categoryName = categoryName;
      this.level = level;
      this.message = message;
      this.exception = exception;
      this.logger = logger;
    }

    return LoggingEvent;

  })();

}).call(this);

(function() {

  namespace("log.impl");

  log.impl.LogManager = (function() {

    function LogManager() {}

    LogManager.prototype.loggerMap = {};

    LogManager.prototype.getLogger = function(name) {
      this.loggerMap[name] = this.loggerMap[name] || new log.impl.Logger(name);
      return this.loggerMap[name];
    };

    return LogManager;

  })();

}).call(this);

(function() {

  namespace("log.layout");

  log.layout.AbstractLayout = (function() {

    function AbstractLayout() {}

    AbstractLayout.prototype.format = function(loggingEvent) {
      throw new Error("please implement the format method in the superclass");
    };

    return AbstractLayout;

  })();

}).call(this);

(function() {
  var __hasProp = Object.prototype.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  namespace("log.layout");

  /*
     MessageLayout consists of the log message itself. For example,
  
     <pre>
             Hello world
     </pre>
  */

  log.layout.MessageLayout = (function(_super) {

    __extends(MessageLayout, _super);

    function MessageLayout() {
      MessageLayout.__super__.constructor.apply(this, arguments);
    }

    MessageLayout.prototype.format = function(loggingEvent) {
      return "" + loggingEvent.message;
    };

    return MessageLayout;

  })(log.layout.AbstractLayout);

}).call(this);

(function() {
  var __hasProp = Object.prototype.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  namespace("log.layout");

  /*
     SimpleLayout consists of the level of the log statement,
     followed by " - " and then the log message itself. For example,
  
     <pre>
             DEBUG - Hello world
     </pre>
  */

  log.layout.SimpleLayout = (function(_super) {

    __extends(SimpleLayout, _super);

    function SimpleLayout() {
      SimpleLayout.__super__.constructor.apply(this, arguments);
    }

    SimpleLayout.prototype.format = function(loggingEvent) {
      return "" + (loggingEvent.level.toString()) + " - " + loggingEvent.message;
    };

    return SimpleLayout;

  })(log.layout.AbstractLayout);

}).call(this);

(function() {

  namespace("log");

  log.Level = (function() {

    Level.prototype.level = null;

    Level.prototype.levelStr = null;

    function Level(level, levelStr) {
      this.level = level;
      this.levelStr = levelStr;
    }

    Level.OFF_INT = Number.MAX_VALUE;

    Level.FATAL_INT = 50000;

    Level.ERROR_INT = 40000;

    Level.WARN_INT = 30000;

    Level.INFO_INT = 20000;

    Level.DEBUG_INT = 10000;

    Level.TRACE_INT = 5000;

    Level.ALL_INT = Number.MIN_VALUE;

    Level.OFF = new Level(Level.OFF_INT, "OFF");

    Level.FATAL = new Level(Level.FATAL_INT, "FATAL");

    Level.ERROR = new Level(Level.ERROR_INT, "ERROR");

    Level.WARN = new Level(Level.WARN_INT, "WARN");

    Level.INFO = new Level(Level.INFO_INT, "INFO");

    Level.DEBUG = new Level(Level.DEBUG_INT, "DEBUG");

    Level.TRACE = new Level(Level.TRACE_INT, "TRACE");

    Level.ALL = new Level(Level.ALL_INT, "ALL");

    Level.prototype.toString = function() {
      return this.levelStr;
    };

    Level.prototype.valueOf = function() {
      return this.level;
    };

    return Level;

  })();

}).call(this);

(function() {

  window.Log = (function() {
    var _logManager;

    function Log() {}

    _logManager = new log.impl.LogManager();

    Log.getLogger = function(name) {
      return _logManager.getLogger(name);
    };

    return Log;

  })();

}).call(this);

