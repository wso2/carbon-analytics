'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.styles = undefined;

var _defineProperty2 = require('babel-runtime/helpers/defineProperty');

var _defineProperty3 = _interopRequireDefault(_defineProperty2);

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _ref;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _classnames = require('classnames');

var _classnames2 = _interopRequireDefault(_classnames);

var _withStyles = require('../styles/withStyles');

var _withStyles2 = _interopRequireDefault(_withStyles);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var styles = exports.styles = {
  root: {
    backgroundSize: 'cover',
    backgroundRepeat: 'no-repeat',
    backgroundPosition: 'center'
  }
};

var babelPluginFlowReactPropTypes_proptype_Props = {
  classes: require('prop-types').object,
  className: require('prop-types').string,
  image: require('prop-types').string.isRequired,
  style: require('prop-types').object
};


function CardMedia(props) {
  var classes = props.classes,
      className = props.className,
      image = props.image,
      style = props.style,
      other = (0, _objectWithoutProperties3.default)(props, ['classes', 'className', 'image', 'style']);

  var composedStyle = (0, _extends3.default)({ backgroundImage: 'url(' + image + ')' }, style);

  return _react2.default.createElement('div', (0, _extends3.default)({ className: (0, _classnames2.default)(classes.root, className), style: composedStyle }, other));
}

CardMedia.propTypes = process.env.NODE_ENV !== "production" ? (_ref = {
  classes: require('prop-types').object.isRequired
}, (0, _defineProperty3.default)(_ref, 'classes', require('prop-types').object), (0, _defineProperty3.default)(_ref, 'className', require('prop-types').string), (0, _defineProperty3.default)(_ref, 'image', require('prop-types').string.isRequired), (0, _defineProperty3.default)(_ref, 'style', require('prop-types').object), _ref) : {};
exports.default = (0, _withStyles2.default)(styles, { name: 'MuiCardMedia' })(CardMedia);