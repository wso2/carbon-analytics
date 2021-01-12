import ReactDOM from 'react-dom';
import Parser from '@asyncapi/parser'
import React from 'react';
import App from './App'

const getAsyncAPIUI = (element, asyncYaml) => {
    ReactDOM.render(<App asyncYaml={asyncYaml}/>, element);
};

const getAsyncAPIParserDoc = (content) => {
    return Parser.parse(content);
}

window.getAsyncAPIUI = getAsyncAPIUI;
window.getAsyncAPIParserDoc = getAsyncAPIParserDoc;
