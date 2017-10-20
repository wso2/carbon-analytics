'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.styles = undefined;

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _defineProperty2 = require('babel-runtime/helpers/defineProperty');

var _defineProperty3 = _interopRequireDefault(_defineProperty2);

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _classnames = require('classnames');

var _classnames2 = _interopRequireDefault(_classnames);

var _withStyles = require('../styles/withStyles');

var _withStyles2 = _interopRequireDefault(_withStyles);

var _helpers = require('../utils/helpers');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var RADIUS = 12; //  weak

var styles = exports.styles = function styles(theme) {
  return {
    root: {
      position: 'relative',
      display: 'inline-block'
    },
    badge: {
      display: 'flex',
      flexDirection: 'row',
      flexWrap: 'wrap',
      justifyContent: 'center',
      alignContent: 'center',
      alignItems: 'center',
      position: 'absolute',
      top: -RADIUS,
      right: -RADIUS,
      fontFamily: theme.typography.fontFamily,
      fontWeight: theme.typography.fontWeight,
      fontSize: RADIUS,
      width: RADIUS * 2,
      height: RADIUS * 2,
      borderRadius: '50%',
      backgroundColor: theme.palette.color,
      color: theme.palette.textColor
    },
    colorPrimary: {
      backgroundColor: theme.palette.primary[500],
      color: theme.palette.getContrastText(theme.palette.primary[500])
    },
    colorAccent: {
      backgroundColor: theme.palette.secondary.A200,
      color: theme.palette.getContrastText(theme.palette.secondary.A200)
    }
  };
};

function Badge(props) {
  var badgeContent = props.badgeContent,
      classes = props.classes,
      classNameProp = props.className,
      color = props.color,
      children = props.children,
      other = (0, _objectWithoutProperties3.default)(props, ['badgeContent', 'classes', 'className', 'color', 'children']);

  var className = (0, _classnames2.default)(classes.root, classNameProp);
  var badgeClassName = (0, _classnames2.default)(classes.badge, (0, _defineProperty3.default)({}, classes['color' + (0, _helpers.capitalizeFirstLetter)(color)], color !== 'default'));

  return _react2.default.createElement(
    'div',
    (0, _extends3.default)({ className: className }, other),
    children,
    _react2.default.createElement(
      'span',
      { className: badgeClassName },
      badgeContent
    )
  );
}

Badge.propTypes = process.env.NODE_ENV !== "production" ? {
  /**
   * The content rendered within the badge.
   */
  badgeContent: _propTypes2.default.node.isRequired,
  /**
   * The badge will be added relative to this node.
   */
  children: _propTypes2.default.node.isRequired,
  /**
   * Useful to extend the style applied to components.
   */
  classes: _propTypes2.default.object.isRequired,
  /**
   * @ignore
   */
  className: _propTypes2.default.string,
  /**
   * The color of the component. It's using the theme palette when that makes sense.
   */
  color: _propTypes2.default.oneOf(['default', 'primary', 'accent'])
} : {};

Badge.defaultProps = {
  color: 'default'
};

exports.default = (0, _withStyles2.default)(styles, { name: 'MuiBadge' })(Badge);