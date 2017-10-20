'use strict';

var babelPluginFlowReactPropTypes_proptype_Node = require('react').babelPluginFlowReactPropTypes_proptype_Node || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_ChildrenArray = require('react').babelPluginFlowReactPropTypes_proptype_ChildrenArray || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Breakpoint = require('../styles/createBreakpoints').babelPluginFlowReactPropTypes_proptype_Breakpoint || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_HiddenProps = {
  children: typeof $ReadOnlyArray === 'function' ? require('prop-types').instanceOf($ReadOnlyArray) : require('prop-types').any,
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
  xlDown: require('prop-types').bool
};