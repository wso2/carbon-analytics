'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

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

exports.default = withTheme;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _createEagerFactory = require('recompose/createEagerFactory');

var _createEagerFactory2 = _interopRequireDefault(_createEagerFactory);

var _wrapDisplayName = require('recompose/wrapDisplayName');

var _wrapDisplayName2 = _interopRequireDefault(_wrapDisplayName);

var _createMuiTheme = require('./createMuiTheme');

var _createMuiTheme2 = _interopRequireDefault(_createMuiTheme);

var _themeListener = require('./themeListener');

var _themeListener2 = _interopRequireDefault(_themeListener);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var babelPluginFlowReactPropTypes_proptype_ComponentType = require('prop-types').func; //  weak

var defaultTheme = void 0;

function getDefaultTheme() {
  if (defaultTheme) {
    return defaultTheme;
  }

  defaultTheme = (0, _createMuiTheme2.default)();
  return defaultTheme;
}

// Provide the theme object as a property to the input component.
function withTheme(BaseComponent) {
  var factory = (0, _createEagerFactory2.default)(BaseComponent);

  var WithTheme = function (_React$Component) {
    (0, _inherits3.default)(WithTheme, _React$Component);

    function WithTheme(props, context) {
      (0, _classCallCheck3.default)(this, WithTheme);

      var _this = (0, _possibleConstructorReturn3.default)(this, (WithTheme.__proto__ || (0, _getPrototypeOf2.default)(WithTheme)).call(this, props, context));

      _this.state = {};
      _this.unsubscribeId = null;

      _this.state = {
        // We use || as it's lazy evaluated.
        theme: _themeListener2.default.initial(context) || getDefaultTheme()
      };
      return _this;
    }

    // Exposed for test purposes.


    (0, _createClass3.default)(WithTheme, [{
      key: 'componentDidMount',
      value: function componentDidMount() {
        var _this2 = this;

        this.unsubscribeId = _themeListener2.default.subscribe(this.context, function (theme) {
          _this2.setState({ theme: theme });
        });
      }
    }, {
      key: 'componentWillUnmount',
      value: function componentWillUnmount() {
        if (this.unsubscribeId !== null) {
          _themeListener2.default.unsubscribe(this.context, this.unsubscribeId);
        }
      }
    }, {
      key: 'render',
      value: function render() {
        return factory((0, _extends3.default)({ theme: this.state.theme }, this.props));
      }
    }]);
    return WithTheme;
  }(_react2.default.Component);

  WithTheme.displayName = (0, _wrapDisplayName2.default)(BaseComponent, 'withTheme');
  WithTheme.Naked = BaseComponent;


  WithTheme.contextTypes = _themeListener2.default.contextTypes;

  return WithTheme;
}