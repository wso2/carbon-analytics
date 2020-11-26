module.exports = {
    presets:
        [
            [
                '@babel/env',
                {
                    // See: https://material-ui.com/getting-started/supported-platforms/ 
                    targets: {ie: '11', edge: '14', firefox: '52', chrome: '49', safari: '10'},
                    useBuiltIns: 'usage',
                },
            ],
            '@babel/preset-react',
        ],
};