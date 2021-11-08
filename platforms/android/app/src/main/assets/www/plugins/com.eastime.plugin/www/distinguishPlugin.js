cordova.define("com.eastime.plugin.distinguishPlugin", function(require, exports, module) {
var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'distinguishPlugin', 'distinguish', [arg0]);
};

});
