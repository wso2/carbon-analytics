'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _defineProperty2 = require('babel-runtime/helpers/defineProperty');

var _defineProperty3 = _interopRequireDefault(_defineProperty2);

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _toConsumableArray2 = require('babel-runtime/helpers/toConsumableArray');

var _toConsumableArray3 = _interopRequireDefault(_toConsumableArray2);

var _getPrototypeOf = require('babel-runtime/core-js/object/get-prototype-of');

var _getPrototypeOf2 = _interopRequireDefault(_getPrototypeOf);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require('babel-runtime/helpers/possibleConstructorReturn');

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require('babel-runtime/helpers/inherits');

var _inherits3 = _interopRequireDefault(_inherits2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _classnames = require('classnames');

var _classnames2 = _interopRequireDefault(_classnames);

var _keycode = require('keycode');

var _keycode2 = _interopRequireDefault(_keycode);

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _Menu = require('../Menu/Menu');

var _Menu2 = _interopRequireDefault(_Menu);

var _Input = require('../Input/Input');

var _ArrowDropDown = require('../svg-icons/ArrowDropDown');

var _ArrowDropDown2 = _interopRequireDefault(_ArrowDropDown);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var babelPluginFlowReactPropTypes_proptype_Node = require('react').babelPluginFlowReactPropTypes_proptype_Node || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Element = require('react').babelPluginFlowReactPropTypes_proptype_Element || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_ChildrenArray = require('react').babelPluginFlowReactPropTypes_proptype_ChildrenArray || require('prop-types').any;

var babelPluginFlowReactPropTypes_proptype_Props = {
  children: typeof $ReadOnlyArray === 'function' ? require('prop-types').instanceOf($ReadOnlyArray).isRequired : require('prop-types').any.isRequired,
  classes: require('prop-types').object.isRequired,
  className: require('prop-types').string,
  disabled: require('prop-types').bool,
  native: require('prop-types').bool.isRequired,
  multiple: require('prop-types').bool.isRequired,
  MenuProps: require('prop-types').object,
  name: require('prop-types').string,
  onBlur: require('prop-types').func,
  onChange: require('prop-types').func,
  onFocus: require('prop-types').func,
  readOnly: require('prop-types').bool,
  renderValue: require('prop-types').func,
  selectRef: require('prop-types').func,
  value: require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number, require('prop-types').arrayOf(require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number]))])
};

/**
 * @ignore - internal component.
 */
var SelectInput = function (_React$Component) {
  (0, _inherits3.default)(SelectInput, _React$Component);

  function SelectInput() {
    var _ref;

    var _temp, _this, _ret;

    (0, _classCallCheck3.default)(this, SelectInput);

    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    return _ret = (_temp = (_this = (0, _possibleConstructorReturn3.default)(this, (_ref = SelectInput.__proto__ || (0, _getPrototypeOf2.default)(SelectInput)).call.apply(_ref, [this].concat(args))), _this), _this.state = {
      anchorEl: null,
      open: false
    }, _this.ignoreNextBlur = false, _this.handleClick = function (event) {
      // Opening the menu is going to blur the. It will be focused back when closed.
      _this.ignoreNextBlur = true;
      _this.setState({
        open: true,
        anchorEl: event.currentTarget
      });
    }, _this.handleRequestClose = function () {
      _this.setState({
        open: false
      });
    }, _this.handleItemClick = function (child) {
      return function (event) {
        if (!_this.props.multiple) {
          _this.setState({
            open: false
          });
        }

        if (_this.props.onChange) {
          var _value = void 0;

          if (_this.props.multiple) {
            _value = Array.isArray(_this.props.value) ? [].concat((0, _toConsumableArray3.default)(_this.props.value)) : [];
            var itemIndex = _value.indexOf(child.props.value);
            if (itemIndex === -1) {
              _value.push(child.props.value);
            } else {
              _value.splice(itemIndex, 1);
            }
          } else {
            _value = child.props.value;
          }

          _this.props.onChange((0, _extends3.default)({}, event, {
            target: {
              value: _value
            }
          }), child);
        }
      };
    }, _this.handleBlur = function (event) {
      if (_this.ignoreNextBlur === true) {
        // The parent components are relying on the bubbling of the event.
        event.stopPropagation();
        _this.ignoreNextBlur = false;
        return;
      }

      if (_this.props.onBlur) {
        _this.props.onBlur(event);
      }
    }, _this.handleKeyDown = function (event) {
      if (_this.props.readOnly) {
        return;
      }

      if (['space', 'up', 'down'].includes((0, _keycode2.default)(event))) {
        event.preventDefault();
        // Opening the menu is going to blur the. It will be focused back when closed.
        _this.ignoreNextBlur = true;
        _this.setState({
          open: true,
          anchorEl: event.currentTarget
        });
      }
    }, _this.handleSelectRef = function (node) {
      if (!_this.props.selectRef) {
        return;
      }

      _this.props.selectRef({
        node: node,
        // By pass the native input as we expose a rich object (array).
        value: _this.props.value
      });
    }, _temp), (0, _possibleConstructorReturn3.default)(_this, _ret);
  }

  (0, _createClass3.default)(SelectInput, [{
    key: 'render',
    value: function render() {
      var _this2 = this;

      var _props = this.props,
          children = _props.children,
          classNameProp = _props.className,
          classes = _props.classes,
          disabled = _props.disabled,
          name = _props.name,
          native = _props.native,
          multiple = _props.multiple,
          _props$MenuProps = _props.MenuProps,
          MenuProps = _props$MenuProps === undefined ? {} : _props$MenuProps,
          onBlur = _props.onBlur,
          onChange = _props.onChange,
          onFocus = _props.onFocus,
          readOnly = _props.readOnly,
          renderValue = _props.renderValue,
          selectRef = _props.selectRef,
          value = _props.value,
          other = (0, _objectWithoutProperties3.default)(_props, ['children', 'className', 'classes', 'disabled', 'name', 'native', 'multiple', 'MenuProps', 'onBlur', 'onChange', 'onFocus', 'readOnly', 'renderValue', 'selectRef', 'value']);


      if (native) {
        process.env.NODE_ENV !== "production" ? (0, _warning2.default)(multiple === false, 'Material-UI: you can not use the `native` and `multiple` properties ' + 'at the same time on a `Select` component.') : void 0;

        return _react2.default.createElement(
          'div',
          { className: classes.root },
          _react2.default.createElement(
            'select',
            (0, _extends3.default)({
              className: (0, _classnames2.default)(classes.select, (0, _defineProperty3.default)({}, classes.disabled, disabled), classNameProp),
              name: name,
              disabled: disabled,
              ref: selectRef,
              onBlur: onBlur,
              onChange: onChange,
              onFocus: onFocus,
              value: value,
              readOnly: readOnly
            }, other),
            children
          ),
          _react2.default.createElement(_ArrowDropDown2.default, { className: classes.icon })
        );
      }

      if (value === undefined) {
        throw new Error('Material-UI: the `value` property is required ' + 'when using the `Select` component with `native=false`.');
      }

      var display = void 0;
      var displaySingle = '';
      var displayMultiple = [];
      var computeDisplay = false;

      // No need to display any value if the field is empty.
      if ((0, _Input.isDirty)(this.props)) {
        if (renderValue) {
          display = renderValue(value);
        } else {
          computeDisplay = true;
        }
      }

      var items = _react2.default.Children.map(children, function (child) {
        if (!_react2.default.isValidElement(child)) {
          return null;
        }
        var selected = void 0;

        if (multiple) {
          if (!Array.isArray(value)) {
            throw new Error('Material-UI: the `value` property must be an array ' + 'when using the `Select` component with `multiple`.');
          }

          selected = value.indexOf(child.props.value) !== -1;
          if (selected && computeDisplay) {
            displayMultiple.push(child.props.children);
          }
        } else {
          selected = value === child.props.value;
          if (selected && computeDisplay) {
            displaySingle = child.props.children;
          }
        }

        return _react2.default.cloneElement(child, {
          role: 'option',
          selected: selected,
          onClick: _this2.handleItemClick(child)
        });
      });

      if (computeDisplay) {
        display = multiple ? displayMultiple.join(', ') : displaySingle;
      }

      return _react2.default.createElement(
        'div',
        { className: classes.root },
        _react2.default.createElement(
          'div',
          {
            className: (0, _classnames2.default)(classes.select, classes.selectMenu, (0, _defineProperty3.default)({}, classes.disabled, disabled), classNameProp),

            'aria-pressed': this.state.open ? 'true' : 'false',
            tabIndex: disabled ? null : 0,
            role: 'button',
            'aria-owns': this.state.open ? 'menu-' + (name || '') : null,
            'aria-haspopup': 'true',
            onKeyDown: this.handleKeyDown,
            onBlur: this.handleBlur,
            onClick: disabled || readOnly ? null : this.handleClick,
            onFocus: onFocus
          },
          display
        ),
        _react2.default.createElement('input', (0, _extends3.default)({
          ref: this.handleSelectRef,
          value: Array.isArray(value) ? value.join(',') : value,
          name: name,
          readOnly: readOnly
        }, other, {
          type: 'hidden'
        })),
        _react2.default.createElement(_ArrowDropDown2.default, { className: classes.icon }),
        _react2.default.createElement(
          _Menu2.default,
          (0, _extends3.default)({
            id: 'menu-' + (name || ''),
            anchorEl: this.state.anchorEl,
            open: this.state.open,
            onRequestClose: this.handleRequestClose
          }, MenuProps, {
            MenuListProps: (0, _extends3.default)({}, MenuProps.MenuListProps, {
              role: 'listbox'
            })
          }),
          items
        )
      );
    }
  }]);
  return SelectInput;
}(_react2.default.Component);

SelectInput.muiName = 'SelectInput';
SelectInput.propTypes = process.env.NODE_ENV !== "production" ? {
  children: typeof $ReadOnlyArray === 'function' ? require('prop-types').instanceOf($ReadOnlyArray).isRequired : require('prop-types').any.isRequired,
  classes: require('prop-types').object.isRequired,
  className: require('prop-types').string,
  disabled: require('prop-types').bool,
  native: require('prop-types').bool.isRequired,
  multiple: require('prop-types').bool.isRequired,
  MenuProps: require('prop-types').object,
  name: require('prop-types').string,
  onBlur: require('prop-types').func,
  onChange: require('prop-types').func,
  onFocus: require('prop-types').func,
  readOnly: require('prop-types').bool,
  renderValue: require('prop-types').func,
  selectRef: require('prop-types').func,
  value: require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number, require('prop-types').arrayOf(require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number]))])
} : {};
SelectInput.propTypes = process.env.NODE_ENV !== "production" ? {
  children: typeof $ReadOnlyArray === 'function' ? require('prop-types').instanceOf($ReadOnlyArray).isRequired : require('prop-types').any.isRequired,
  classes: require('prop-types').object.isRequired,
  className: require('prop-types').string,
  disabled: require('prop-types').bool,
  native: require('prop-types').bool.isRequired,
  multiple: require('prop-types').bool.isRequired,
  MenuProps: require('prop-types').object,
  name: require('prop-types').string,
  onBlur: require('prop-types').func,
  onChange: require('prop-types').func,
  onFocus: require('prop-types').func,
  readOnly: require('prop-types').bool,
  renderValue: require('prop-types').func,
  selectRef: require('prop-types').func,
  value: require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number, require('prop-types').arrayOf(require('prop-types').oneOfType([require('prop-types').string, require('prop-types').number]))])
} : {};
exports.default = SelectInput;