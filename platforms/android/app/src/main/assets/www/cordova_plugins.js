cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "com.eastime.plugin.distinguishPlugin",
      "file": "plugins/com.eastime.plugin/www/distinguishPlugin.js",
      "pluginId": "com.eastime.plugin",
      "clobbers": [
        "cordova.distinguishPlugin"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.5",
    "com.eastime.plugin": "1.0.0"
  };
});