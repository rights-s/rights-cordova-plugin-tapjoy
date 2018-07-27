var exec = require('cordova/exec');

var Tapjoy = {
  setup: function(success_cb, error_cb, debugMode, userID, appKey) {
    exec(success_cb, error_cb, 'PluginTapjoy', 'setup', [debugMode, userID, appKey]);
  },
  setUserID: function(userID) {
    exec(null, null, 'PluginTapjoy', 'setUserID', [userID]);
  },
  setUserLevel: function(userLevel) {
    exec(null, null, 'PluginTapjoy', 'setUserLevel', [userLevel]);
  },
  setUserCohortVariable: function(orderNum, value) {
    exec(null, null, 'PluginTapjoy', 'setUserCohortVariable', [orderNum, value]);
  },
  createPlacement: function(success_cb, error_cb, name){
    exec(success_cb, error_cb, 'PluginTapjoy', 'createPlacement', [name]);
  },
  requestContent: function(success_cb, error_cb, name){
    exec(success_cb, error_cb, 'PluginTapjoy', 'requestContent', [name]);
  },
  showContent: function(success_cb, error_cb, name){
    exec(success_cb, error_cb, 'PluginTapjoy', 'showContent', [name]);
  }
};

module.exports = Tapjoy;
