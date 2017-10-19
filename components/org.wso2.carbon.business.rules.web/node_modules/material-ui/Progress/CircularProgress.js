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

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

//  weak

var THICKNESS = 3.6;
var PI = 3.1416; // Simple version of Math.PI for the CSS generated.

function getRelativeValue(value, min, max) {
  var clampedValue = Math.min(Math.max(min, value), max);
  return (clampedValue - min) / (max - min);
}

var styles = exports.styles = function styles(theme) {
  return {
    root: {
      display: 'inline-block'
    },
    primaryColor: {
      color: theme.palette.primary[500]
    },
    accentColor: {
      color: theme.palette.secondary.A200
    },
    svg: {
      transform: 'rotate(-90deg)'
    },
    indeterminateSvg: {
      // The main animation is loop 4 times (4 / 3 * 1300).
      animation: 'mui-rotate-progress-circle 1733ms linear infinite'
    },
    circle: {
      stroke: 'currentColor',
      strokeLinecap: 'square',
      transition: theme.transitions.create('all', { duration: 1300 })
    },
    indeterminateCircle: {
      strokeDasharray: '1, calc((100% - ' + THICKNESS + 'px) * ' + PI + ')',
      strokeDashoffset: '0%',
      animation: 'mui-scale-progress-circle 1300ms ' + theme.transitions.easing.easeInOut + ' infinite'
    },
    determinateCircle: {
      willChange: 'strokeDasharray',
      strokeDashoffset: '0%'
    },
    '@keyframes mui-rotate-progress-circle': {
      '0%': {
        transform: 'rotate(-90deg)'
      },
      '100%': {
        transform: 'rotate(270deg)'
      }
    },
    '@keyframes mui-scale-progress-circle': {
      '8%': {
        strokeDasharray: '1, calc((100% - ' + THICKNESS + 'px) * ' + PI + ')',
        strokeDashoffset: 0
      },
      '50%, 58%': {
        // eslint-disable-next-line max-len
        strokeDasharray: 'calc((65% - ' + THICKNESS + 'px) * ' + PI + '), calc((100% - ' + THICKNESS + 'px) * ' + PI + ')',
        strokeDashoffset: 'calc((25% - ' + THICKNESS + 'px) * -' + PI + ')'
      },
      '100%': {
        // eslint-disable-next-line max-len
        strokeDasharray: 'calc((65% - ' + THICKNESS + 'px) * ' + PI + '), calc((100% - ' + THICKNESS + 'px) * ' + PI + ')',
        strokeDashoffset: 'calc((99% - ' + THICKNESS + 'px) * -' + PI + ')'
      }
    }
  };
};

function CircularProgress(props) {
  var _classNames2;

  var classes = props.classes,
      className = props.className,
      color = props.color,
      size = props.size,
      mode = props.mode,
      value = props.value,
      min = props.min,
      max = props.max,
      other = (0, _objectWithoutProperties3.default)(props, ['classes', 'className', 'color', 'size', 'mode', 'value', 'min', 'max']);

  var radius = size / 2;
  var rootProps = {};
  var svgClassName = (0, _classnames2.default)(classes.svg, (0, _defineProperty3.default)({}, classes.indeterminateSvg, mode === 'indeterminate'));

  var circleClassName = (0, _classnames2.default)(classes.circle, (_classNames2 = {}, (0, _defineProperty3.default)(_classNames2, classes.indeterminateCircle, mode === 'indeterminate'), (0, _defineProperty3.default)(_classNames2, classes.determinateCircle, mode === 'determinate'), _classNames2));

  var circleStyle = {};
  if (mode === 'determinate') {
    var relVal = getRelativeValue(value, min, max);
    circleStyle.strokeDasharray = 'calc(((100% - ' + THICKNESS + 'px) * ' + PI + ') * ' + relVal + '),' + ('calc((100% - ' + THICKNESS + 'px) * ' + PI + ')');
    rootProps['aria-valuenow'] = value;
    rootProps['aria-valuemin'] = min;
    rootProps['aria-valuemax'] = max;
  }

  var colorClass = classes[color + 'Color'];

  return _react2.default.createElement(
    'div',
    (0, _extends3.default)({
      className: (0, _classnames2.default)(classes.root, colorClass, className),
      style: { width: size, height: size },
      role: 'progressbar'
    }, rootProps, other),
    _react2.default.createElement(
      'svg',
      { className: svgClassName, viewBox: '0 0 ' + size + ' ' + size },
      _react2.default.createElement('circle', {
        className: circleClassName,
        style: circleStyle,
        cx: radius,
        cy: radius,
        r: radius - THICKNESS / 2,
        fill: 'none',
        strokeWidth: THICKNESS,
        strokeMiterlimit: '20'
      })
    )
  );
}

CircularProgress.propTypes = process.env.NODE_ENV !== "production" ? {
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
  color: _propTypes2.default.oneOf(['primary', 'accent']),
  /**
   * The max value of progress in determinate mode.
   */
  max: _propTypes2.default.number,
  /**
   * The min value of progress in determinate mode.
   */
  min: _propTypes2.default.number,
  /**
   * The mode of show your progress. Indeterminate
   * for when there is no value for progress.
   * Determinate for controlled progress value.
   */
  mode: _propTypes2.default.oneOf(['determinate', 'indeterminate']),
  /**
   * The size of the circle.
   */
  size: _propTypes2.default.number,
  /**
   * The value of progress in determinate mode.
  */
  value: _propTypes2.default.number
} : {};

CircularProgress.defaultProps = {
  color: 'primary',
  size: 40,
  mode: 'indeterminate',
  value: 0,
  min: 0,
  max: 100
};

exports.default = (0, _withStyles2.default)(styles, { name: 'MuiCircularProgress' })(CircularProgress);