'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _Input = require('../Input');

var _Input2 = _interopRequireDefault(_Input);

var _FormControl = require('../Form/FormControl');

var _FormControl2 = _interopRequireDefault(_FormControl);

var _FormHelperText = require('../Form/FormHelperText');

var _FormHelperText2 = _interopRequireDefault(_FormHelperText);

var _Select = require('../Select/Select');

var _Select2 = _interopRequireDefault(_Select);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// @inheritedComponent FormControl

var babelPluginFlowReactPropTypes_proptype_Node = require('react').babelPluginFlowReactPropTypes_proptype_Node || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_ChildrenArray = require('react').babelPluginFlowReactPropTypes_proptype_ChildrenArray || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Props = {
  autoComplete: require('prop-types').string,
  autoFocus: require('prop-types').bool,
  children: typeof babelPluginFlowReactPropTypes_proptype_ChildrenArray === 'function' ? babelPluginFlowReactPropTypes_proptype_ChildrenArray : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_ChildrenArray),
  className: require('prop-types').string,
  defaultValue: require('prop-types').string,
  disabled: require('prop-types').bool,
  error: require('prop-types').bool,
  FormHelperTextProps: require('prop-types').object,
  fullWidth: require('prop-types').bool,
  helperText: typeof babelPluginFlowReactPropTypes_proptype_Node === 'function' ? babelPluginFlowReactPropTypes_proptype_Node : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Node),
  helperTextClassName: require('prop-types').string,
  id: require('prop-types').string,
  inputClassName: require('prop-types').string,
  InputClassName: require('prop-types').string,
  InputLabelProps: require('prop-types').object,
  inputProps: require('prop-types').object,
  InputProps: require('prop-types').object,
  inputRef: require('prop-types').func,
  label: typeof babelPluginFlowReactPropTypes_proptype_Node === 'function' ? babelPluginFlowReactPropTypes_proptype_Node : require('prop-types').shape(babelPluginFlowReactPropTypes_proptype_Node),
  labelClassName: require('prop-types').string,
  multiline: require('prop-types').bool,
  name: require('prop-types').string,
  onChange: require('prop-types').func,
  placeholder: require('prop-types').string,
  required: require('prop-types').bool,
  rootRef: require('prop-types').func,
  rows: require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number]),
  rowsMax: require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number]),
  select: require('prop-types').bool,
  SelectProps: require('prop-types').object,
  type: require('prop-types').string,
  value: require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number]),
  margin: require('prop-types').oneOf(['none', 'dense', 'normal'])
};


function TextField(props) {
  var autoComplete = props.autoComplete,
      autoFocus = props.autoFocus,
      children = props.children,
      className = props.className,
      defaultValue = props.defaultValue,
      disabled = props.disabled,
      error = props.error,
      id = props.id,
      inputClassName = props.inputClassName,
      InputClassName = props.InputClassName,
      inputPropsProp = props.inputProps,
      InputProps = props.InputProps,
      inputRef = props.inputRef,
      label = props.label,
      labelClassName = props.labelClassName,
      InputLabelProps = props.InputLabelProps,
      helperText = props.helperText,
      helperTextClassName = props.helperTextClassName,
      FormHelperTextProps = props.FormHelperTextProps,
      fullWidth = props.fullWidth,
      required = props.required,
      type = props.type,
      multiline = props.multiline,
      name = props.name,
      onChange = props.onChange,
      placeholder = props.placeholder,
      rootRef = props.rootRef,
      rows = props.rows,
      rowsMax = props.rowsMax,
      select = props.select,
      SelectProps = props.SelectProps,
      value = props.value,
      other = (0, _objectWithoutProperties3.default)(props, ['autoComplete', 'autoFocus', 'children', 'className', 'defaultValue', 'disabled', 'error', 'id', 'inputClassName', 'InputClassName', 'inputProps', 'InputProps', 'inputRef', 'label', 'labelClassName', 'InputLabelProps', 'helperText', 'helperTextClassName', 'FormHelperTextProps', 'fullWidth', 'required', 'type', 'multiline', 'name', 'onChange', 'placeholder', 'rootRef', 'rows', 'rowsMax', 'select', 'SelectProps', 'value']);


  var inputProps = inputPropsProp;

  if (inputClassName) {
    inputProps = (0, _extends3.default)({
      className: inputClassName
    }, inputProps);
  }

  process.env.NODE_ENV !== "production" ? (0, _warning2.default)(!select || children, 'Material-UI: `children` must be passed when using the `TextField` component with `select`.') : void 0;

  var InputComponent = _react2.default.createElement(_Input2.default, (0, _extends3.default)({
    autoComplete: autoComplete,
    autoFocus: autoFocus,
    className: InputClassName,
    defaultValue: defaultValue,
    disabled: disabled,
    multiline: multiline,
    name: name,
    rows: rows,
    rowsMax: rowsMax,
    type: type,
    value: value,
    id: id,
    inputProps: inputProps,
    inputRef: inputRef,
    onChange: onChange,
    placeholder: placeholder
  }, InputProps));

  return _react2.default.createElement(
    _FormControl2.default,
    (0, _extends3.default)({
      fullWidth: fullWidth,
      ref: rootRef,
      className: className,
      error: error,
      required: required
    }, other),
    label && _react2.default.createElement(
      _Input.InputLabel,
      (0, _extends3.default)({ htmlFor: id, className: labelClassName }, InputLabelProps),
      label
    ),
    select ? _react2.default.createElement(
      _Select2.default,
      (0, _extends3.default)({ input: InputComponent }, SelectProps),
      children
    ) : InputComponent,
    helperText && _react2.default.createElement(
      _FormHelperText2.default,
      (0, _extends3.default)({ className: helperTextClassName }, FormHelperTextProps),
      helperText
    )
  );
}

TextField.propTypes = process.env.NODE_ENV !== "production" ? babelPluginFlowReactPropTypes_proptype_Props : {};
TextField.defaultProps = {
  required: false,
  select: false
};

exports.default = TextField;