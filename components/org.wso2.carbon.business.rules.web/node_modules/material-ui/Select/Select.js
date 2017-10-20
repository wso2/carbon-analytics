'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.styles = undefined;

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _SelectInput = require('./SelectInput');

var _SelectInput2 = _interopRequireDefault(_SelectInput);

var _withStyles = require('../styles/withStyles');

var _withStyles2 = _interopRequireDefault(_withStyles);

var _Input = require('../Input');

var _Input2 = _interopRequireDefault(_Input);

var _reactHelpers = require('../utils/reactHelpers');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var babelPluginFlowReactPropTypes_proptype_Element = require('react').babelPluginFlowReactPropTypes_proptype_Element || require('prop-types').any;
// @inheritedComponent Input

var babelPluginFlowReactPropTypes_proptype_ChildrenArray = require('react').babelPluginFlowReactPropTypes_proptype_ChildrenArray || require('prop-types').any; // Import to enforce the CSS injection order


var styles = exports.styles = function styles(theme) {
  return {
    root: {
      position: 'relative'
    },
    select: {
      '-moz-appearance': 'none', // Remove Firefox custom style
      '-webkit-appearance': 'none', // Fix SSR issue
      appearance: 'none', // Reset
      // When interacting quickly, the text can end up selected.
      // Native select can't be selected either.
      userSelect: 'none',
      padding: '0 ' + theme.spacing.unit * 4 + 'px 0 0',
      width: 'calc(100% - ' + theme.spacing.unit * 4 + 'px)',
      minWidth: theme.spacing.unit * 2, // So it doesn't collapse.
      height: 'calc(1em + ' + (theme.spacing.unit * 2 - 2) + 'px)',
      cursor: 'pointer',
      '&:focus': {
        // Show that it's not an text input
        background: theme.palette.type === 'light' ? 'rgba(0, 0, 0, 0.05)' : 'rgba(255, 255, 255, 0.05)',
        borderRadius: 0 // Reset Chrome style
      },
      // Remove Firefox focus border
      '&:-moz-focusring': {
        color: 'transparent',
        textShadow: '0 0 0 #000'
      }
    },
    selectMenu: {
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      overflow: 'hidden',
      lineHeight: 'calc(1em + ' + (theme.spacing.unit * 2 - 2) + 'px)'
    },
    disabled: {
      cursor: 'default'
    },
    icon: {
      position: 'absolute',
      right: 0,
      top: 4,
      color: theme.palette.text.secondary,
      'pointer-events': 'none' // Don't block pinter events on the select under the icon.
    }
  };
};

var babelPluginFlowReactPropTypes_proptype_Props = {
  children: typeof babelPluginFlowReactPropTypes_proptype_ChildrenArray === 'function' ? babelPluginFlowReactPropTypes_proptype_ChildrenArray.isRequired ? babelPluginFlowReactPropTypes_proptype_ChildrenArray.isRequired : babelPluginFlowReactPropTypes_proptype_ChildrenArray : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_ChildrenArray).isRequired,
  classes: require('prop-types').object,
  input: typeof babelPluginFlowReactPropTypes_proptype_Element === 'function' ? babelPluginFlowReactPropTypes_proptype_Element : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Element),
  native: require('prop-types').bool,
  multiple: require('prop-types').bool,
  MenuProps: require('prop-types').object,
  renderValue: require('prop-types').func,
  value: require('prop-types').oneOfType([require('prop-types').arrayOf(require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number])), require('prop-types').string, require('prop-types').number])
};


function Select(props) {
  var children = props.children,
      classes = props.classes,
      input = props.input,
      native = props.native,
      multiple = props.multiple,
      MenuProps = props.MenuProps,
      renderValue = props.renderValue,
      other = (0, _objectWithoutProperties3.default)(props, ['children', 'classes', 'input', 'native', 'multiple', 'MenuProps', 'renderValue']);

  // Instead of `Element<typeof Input>` to have more flexibility.

  process.env.NODE_ENV !== "production" ? (0, _warning2.default)((0, _reactHelpers.isMuiElement)(input, ['Input']), ['Material-UI: you have provided an invalid value to the `input` property.', 'We expect an element instance of the `Input` component.'].join('\n')) : void 0;

  return _react2.default.cloneElement(input, (0, _extends3.default)({
    // Most of the logic is implemented in `SelectInput`.
    // The `Select` component is a simple API wrapper to expose something better to play with.
    inputComponent: _SelectInput2.default
  }, other, {
    inputProps: (0, _extends3.default)({}, input.props.inputProps, {
      children: children,
      classes: classes,
      native: native,
      multiple: multiple,
      MenuProps: MenuProps,
      renderValue: renderValue
    })
  }));
}

Select.defaultProps = {
  input: _react2.default.createElement(_Input2.default, null),
  native: false,
  multiple: false
};

Select.muiName = 'Select';

exports.default = (0, _withStyles2.default)(styles, { name: 'MuiSelect' })(Select);