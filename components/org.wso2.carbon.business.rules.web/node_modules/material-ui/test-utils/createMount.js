'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

exports.default = createMount;

var _reactDom = require('react-dom');

var _enzyme = require('enzyme');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// Generate an enhanced mount function.
//  weak

function createMount() {
  var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
  var _options$mount = options.mount,
      mount = _options$mount === undefined ? _enzyme.mount : _options$mount;


  var attachTo = window.document.createElement('div');
  attachTo.className = 'app';
  attachTo.setAttribute('id', 'app');
  window.document.body.insertBefore(attachTo, window.document.body.firstChild);

  var mountWithContext = function mountWithContext(node) {
    var options2 = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    return mount(node, (0, _extends3.default)({
      attachTo: attachTo
    }, options2));
  };

  mountWithContext.attachTo = attachTo;
  mountWithContext.cleanUp = function () {
    (0, _reactDom.unmountComponentAtNode)(attachTo);
    attachTo.parentNode.removeChild(attachTo);
  };

  return mountWithContext;
}