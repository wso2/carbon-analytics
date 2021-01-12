const path = require('path');
module.exports = {
    context: path.resolve(__dirname, './src'),
    entry:  './index.js',
    node: {
        __dirname: true
    },
    output: {
        path: path.resolve(__dirname, './dist/'),
        filename: 'bundle.js',
        // libraryTarget: 'amd',
        // umdNamedDefine: true
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader",
                    options: {
                        presets:['es2015', 'react']
                    }
                }
            },
            { 
                test: /\.css$/, 
                use: ['style-loader', 'css-loader'], 
            },
        ],
    },
    resolve: { 
        extensions: ['.js', '.jsx'], 
    },
};