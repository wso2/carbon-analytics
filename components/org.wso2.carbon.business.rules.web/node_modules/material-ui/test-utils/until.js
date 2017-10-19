'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

exports.default = until;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

//  weak

function shallowRecursively(wrapper, selector, _ref) {
  var context = _ref.context;

  if (wrapper.isEmptyRender() || typeof wrapper.node.type === 'string') {
    return wrapper;
  }

  var newContext = context;

  var instance = wrapper.root.instance();
  if (instance.getChildContext) {
    newContext = (0, _extends3.default)({}, context, instance.getChildContext());
  }

  var nextWrapper = wrapper.shallow({ context: newContext });

  if (selector && wrapper.is(selector)) {
    return nextWrapper;
  }

  return shallowRecursively(nextWrapper, selector, { context: newContext });
}

function until(selector) {
  var _this = this;

  var _ref2 = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : this.options,
      context = _ref2.context;

  return this.single('until', function () {
    return shallowRecursively(_this, selector, { context: context });
  });
}