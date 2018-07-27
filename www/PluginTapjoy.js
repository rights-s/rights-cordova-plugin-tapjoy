var exec = require('cordova/exec');

var Tapjoy = {
  setup: function(success_cb, error_cb, debugMode, userID, appKey) {
    exec(success_cb, error_cb, 'PluginTapjoy', 'setup', [debugMode, userID, appKey]);
  },
  setUserID: function(success_cb, error_cb, userID) {
    exec(success_cb, error_cb, 'PluginTapjoy', 'setUserID', [userID]);
  },
  setUserLevel: function(success_cb, error_cb, userLevel) {
    exec(success_cb, error_cb, 'PluginTapjoy', 'setUserLevel', [userLevel]);
  },
  setUserCohortVariable: function(success_cb, error_cb, orderNum, value) {
    exec(success_cb, error_cb, 'PluginTapjoy', 'setUserCohortVariable', [orderNum, value]);
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
