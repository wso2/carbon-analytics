'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.RadioDocs = exports.styles = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _withStyles = require('../styles/withStyles');

var _withStyles2 = _interopRequireDefault(_withStyles);

var _SwitchBase = require('../internal/SwitchBase');

var _SwitchBase2 = _interopRequireDefault(_SwitchBase);

var _RadioButtonChecked = require('../svg-icons/RadioButtonChecked');

var _RadioButtonChecked2 = _interopRequireDefault(_RadioButtonChecked);

var _RadioButtonUnchecked = require('../svg-icons/RadioButtonUnchecked');

var _RadioButtonUnchecked2 = _interopRequireDefault(_RadioButtonUnchecked);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var babelPluginFlowReactPropTypes_proptype_Node = require('react').babelPluginFlowReactPropTypes_proptype_Node || require('prop-types').any; //  weak

var styles = exports.styles = function styles(theme) {
  return {
    default: {
      color: theme.palette.text.secondary
    },
    checked: {
      color: theme.palette.primary[500]
    },
    disabled: {
      color: theme.palette.action.disabled
    }
  };
};

var Radio = (0, _withStyles2.default)(styles, { name: 'MuiRadio' })((0, _SwitchBase2.default)({
  inputType: 'radio',
  defaultIcon: _react2.default.createElement(_RadioButtonUnchecked2.default, null),
  defaultCheckedIcon: _react2.default.createElement(_RadioButtonChecked2.default, null)
}));

Radio.displayName = 'Radio';

exports.default = Radio;
var babelPluginFlowReactPropTypes_proptype_Props = {
  checked: require('prop-types').oneOfType([require('prop-types').bool, require('prop-types').string]),
  checkedClassName: require('prop-types').string,
  checkedIcon: typeof babelPluginFlowReactPropTypes_proptype_Node === 'function' ? babelPluginFlowReactPropTypes_proptype_Node : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Node),
  classes: require('prop-types').object,
  className: require('prop-types').string,
  defaultChecked: require('prop-types').bool,
  disabled: require('prop-types').bool,
  disabledClassName: require('prop-types').string,
  disableRipple: require('prop-types').bool,
  icon: typeof babelPluginFlowReactPropTypes_proptype_Node === 'function' ? babelPluginFlowReactPropTypes_proptype_Node : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Node),
  inputProps: require('prop-types').object,
  inputRef: require('prop-types').func,
  name: require('prop-types').string,
  onChange: require('prop-types').func,
  tabIndex: require('prop-types').oneOfType([require('prop-types').number, require('prop-types').string]),
  value: require('prop-types').string
};

var _ref = _react2.default.createElement('span', null);

// This is here solely to trigger api doc generation
var RadioDocs = exports.RadioDocs = function RadioDocs(props) {
  return _ref;
}; // eslint-disable-line no-unused-vars

RadioDocs.propTypes = process.env.NODE_ENV !== "production" ? babelPluginFlowReactPropTypes_proptype_Props : {};