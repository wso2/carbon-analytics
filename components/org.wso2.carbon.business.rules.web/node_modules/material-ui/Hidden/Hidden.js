'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _HiddenJs = require('./HiddenJs');

var _HiddenJs2 = _interopRequireDefault(_HiddenJs);

var _HiddenCss = require('./HiddenCss');

var _HiddenCss2 = _interopRequireDefault(_HiddenCss);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var babelPluginFlowReactPropTypes_proptype_Node = require('react').babelPluginFlowReactPropTypes_proptype_Node || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Breakpoint = require('../styles/createBreakpoints').babelPluginFlowReactPropTypes_proptype_Breakpoint || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Props = {
  children: typeof babelPluginFlowReactPropTypes_proptype_Node === 'function' ? babelPluginFlowReactPropTypes_proptype_Node : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Node),
  className: require('prop-types').string,
  only: require('prop-types').oneOfType([typeof babelPluginFlowReactPropTypes_proptype_Breakpoint === 'function' ? babelPluginFlowReactPropTypes_proptype_Breakpoint : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Breakpoint), require('prop-types').arrayOf(typeof babelPluginFlowReactPropTypes_proptype_Breakpoint === 'function' ? babelPluginFlowReactPropTypes_proptype_Breakpoint : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Breakpoint))]),
  xsUp: require('prop-types').bool,
  smUp: require('prop-types').bool,
  mdUp: require('prop-types').bool,
  lgUp: require('prop-types').bool,
  xlUp: require('prop-types').bool,
  xsDown: require('prop-types').bool,
  smDown: require('prop-types').bool,
  mdDown: require('prop-types').bool,
  lgDown: require('prop-types').bool,
  xlDown: require('prop-types').bool,
  implementation: require('prop-types').oneOf(['js', 'css'])
};


/**
 * Responsively hides children based on the selected implementation.
 */
function Hidden(props) {
  var implementation = props.implementation,
      other = (0, _objectWithoutProperties3.default)(props, ['implementation']);


  if (implementation === 'js') {
    return _react2.default.createElement(_HiddenJs2.default, other);
  }

  return _react2.default.createElement(_HiddenCss2.default, other);
}

Hidden.propTypes = process.env.NODE_ENV !== "production" ? babelPluginFlowReactPropTypes_proptype_Props : {};
Hidden.defaultProps = {
  implementation: 'js',
  xsUp: false,
  smUp: false,
  mdUp: false,
  lgUp: false,
  xlUp: false,
  xsDown: false,
  smDown: false,
  mdDown: false,
  lgDown: false,
  xlDown: false
};

exports.default = Hidden;