'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _objectWithoutProperties2 = require('babel-runtime/helpers/objectWithoutProperties');

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

exports.default = createTypography;

var _deepmerge = require('deepmerge');

var _deepmerge2 = _interopRequireDefault(_deepmerge);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// < 1kb payload overhead when lodash/merge is > 3kb.

function createTypography(palette, typography) {
  var _ref = typeof typography === 'function' ? typography(palette) : typography,
      _ref$fontFamily = _ref.fontFamily,
      fontFamily = _ref$fontFamily === undefined ? '"Roboto", "Helvetica", "Arial", sans-serif' : _ref$fontFamily,
      _ref$fontSize = _ref.fontSize,
      fontSize = _ref$fontSize === undefined ? 14 : _ref$fontSize,
      _ref$fontWeightLight = _ref.fontWeightLight,
      fontWeightLight = _ref$fontWeightLight === undefined ? 300 : _ref$fontWeightLight,
      _ref$fontWeightRegula = _ref.fontWeightRegular,
      fontWeightRegular = _ref$fontWeightRegula === undefined ? 400 : _ref$fontWeightRegula,
      _ref$fontWeightMedium = _ref.fontWeightMedium,
      fontWeightMedium = _ref$fontWeightMedium === undefined ? 500 : _ref$fontWeightMedium,
      other = (0, _objectWithoutProperties3.default)(_ref, ['fontFamily', 'fontSize', 'fontWeightLight', 'fontWeightRegular', 'fontWeightMedium']);

  return (0, _deepmerge2.default)({
    fontFamily: fontFamily,
    fontSize: fontSize,
    fontWeightLight: fontWeightLight,
    fontWeightRegular: fontWeightRegular,
    fontWeightMedium: fontWeightMedium,
    display4: {
      fontSize: 112,
      fontWeight: fontWeightLight,
      fontFamily: fontFamily,
      letterSpacing: '-.04em',
      lineHeight: 1,
      color: palette.text.secondary
    },
    display3: {
      fontSize: 56,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      letterSpacing: '-.02em',
      lineHeight: 1.35,
      color: palette.text.secondary
    },
    display2: {
      fontSize: 45,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      lineHeight: '48px',
      color: palette.text.secondary
    },
    display1: {
      fontSize: 34,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      lineHeight: '40px',
      color: palette.text.secondary
    },
    headline: {
      fontSize: 24,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      lineHeight: '32px',
      color: palette.text.primary
    },
    title: {
      fontSize: 21,
      fontWeight: fontWeightMedium,
      fontFamily: fontFamily,
      lineHeight: 1,
      color: palette.text.primary
    },
    subheading: {
      fontSize: 16,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      lineHeight: '24px',
      color: palette.text.primary
    },
    body2: {
      fontSize: 14,
      fontWeight: fontWeightMedium,
      fontFamily: fontFamily,
      lineHeight: '24px',
      color: palette.text.primary
    },
    body1: {
      fontSize: 14,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      lineHeight: '20px',
      color: palette.text.primary
    },
    caption: {
      fontSize: 12,
      fontWeight: fontWeightRegular,
      fontFamily: fontFamily,
      lineHeight: 1,
      color: palette.text.secondary
    },
    button: {
      fontSize: fontSize,
      textTransform: 'uppercase',
      fontWeight: fontWeightMedium,
      fontFamily: fontFamily
    }
  }, other);
}