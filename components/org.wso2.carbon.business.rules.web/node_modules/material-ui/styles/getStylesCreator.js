'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _keys = require('babel-runtime/core-js/object/keys');

var _keys2 = _interopRequireDefault(_keys);

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _warning = require('warning');

var _warning2 = _interopRequireDefault(_warning);

var _deepmerge = require('deepmerge');

var _deepmerge2 = _interopRequireDefault(_deepmerge);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// < 1kb payload overhead when lodash/merge is > 3kb.

function getStylesCreator(stylesOrCreator) {
  function create(theme, name) {
    var styles = typeof stylesOrCreator === 'function' ? stylesOrCreator(theme) : stylesOrCreator;

    if (!theme.overrides || !theme.overrides[name]) {
      return styles;
    }

    var overrides = theme.overrides[name];
    var stylesWithOverrides = (0, _extends3.default)({}, styles);

    (0, _keys2.default)(overrides).forEach(function (key) {
      process.env.NODE_ENV !== "production" ? (0, _warning2.default)(stylesWithOverrides[key], 'You are trying to overrides a style that do not exist.') : void 0;
      stylesWithOverrides[key] = (0, _deepmerge2.default)(stylesWithOverrides[key], overrides[key], {
        clone: true // We don't want to mutate the input
      });
    });

    return stylesWithOverrides;
  }

  return {
    create: create,
    options: {
      index: undefined
    },
    themingEnabled: typeof stylesOrCreator === 'function'
  };
}

exports.default = getStylesCreator;