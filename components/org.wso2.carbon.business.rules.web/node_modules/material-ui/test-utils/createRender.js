'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = createRender;

var _enzyme = require('enzyme');

// Generate a render to string function.
function createRender() {
  var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
  var _options$render = options.render,
      render = _options$render === undefined ? _enzyme.render : _options$render;

  var renderWithContext = function renderWithContext(node) {
    var options2 = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    return render(node, options2);
  };

  return renderWithContext;
}