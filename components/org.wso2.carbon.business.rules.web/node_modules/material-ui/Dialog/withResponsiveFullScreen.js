'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _createEagerFactory = require('recompose/createEagerFactory');

var _createEagerFactory2 = _interopRequireDefault(_createEagerFactory);

var _wrapDisplayName = require('recompose/wrapDisplayName');

var _wrapDisplayName2 = _interopRequireDefault(_wrapDisplayName);

var _withWidth = require('../utils/withWidth');

var _withWidth2 = _interopRequireDefault(_withWidth);

var _Dialog = require('./Dialog');

var _Dialog2 = _interopRequireDefault(_Dialog);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var babelPluginFlowReactPropTypes_proptype_Element = require('react').babelPluginFlowReactPropTypes_proptype_Element || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Breakpoint = require('../styles/createBreakpoints').babelPluginFlowReactPropTypes_proptype_Breakpoint || require('prop-types').any;

/**
 * Dialog will responsively be full screen *at or below* the given breakpoint
 * (defaults to 'sm' for mobile devices).
 * Notice that this Higher-order Component is incompatible with server side rendering.
 */
function withResponsiveFullScreen() {
  var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : { breakpoint: 'sm' };
  var breakpoint = options.breakpoint;


  return function (BaseDialog) {
    var factory = (0, _createEagerFactory2.default)(BaseDialog);

    function ResponsiveFullScreen(props) {
      return factory((0, _extends3.default)({
        fullScreen: (0, _withWidth.isWidthDown)(breakpoint, props.width)
      }, props));
    }

    if (process.env.NODE_ENV !== 'production') {
      ResponsiveFullScreen.displayName = (0, _wrapDisplayName2.default)(BaseDialog, 'withResponsiveFullScreen');
    }

    return (0, _withWidth2.default)()(ResponsiveFullScreen);
  };
}

exports.default = withResponsiveFullScreen;