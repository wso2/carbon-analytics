'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

exports.default = createShallow;

var _enzyme = require('enzyme');

var _until = require('./until');

var _until2 = _interopRequireDefault(_until);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// Generate an enhanced shallow function.
//  weak

function createShallow() {
  var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
  var _options$shallow = options.shallow,
      shallow = _options$shallow === undefined ? _enzyme.shallow : _options$shallow,
      context = options.context,
      _options$dive = options.dive,
      dive = _options$dive === undefined ? false : _options$dive,
      _options$untilSelecto = options.untilSelector,
      untilSelector = _options$untilSelecto === undefined ? false : _options$untilSelecto;


  var shallowWithContext = function shallowWithContext(node) {
    var options2 = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
    var context2 = options2.context,
        other = (0, _objectWithoutProperties3.default)(options2, ['context']);


    var wrapper = shallow(node, (0, _extends3.default)({}, other, {
      context: (0, _extends3.default)({}, context, context2)
    }));

    if (dive) {
      return wrapper.dive();
    }

    if (untilSelector) {
      return _until2.default.call(wrapper, untilSelector);
    }

    return wrapper;
  };

  return shallowWithContext;
}