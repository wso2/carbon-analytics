import React from 'react';
import ReactDOM from 'react-dom';
import App3 from './App3';
import App2 from './App2';
import './index.css';

ReactDOM.render(
  <App3 />,
  document.getElementById('root')
);

ReactDOM.render(
  <App2 />,
  document.getElementById('bar')
);
