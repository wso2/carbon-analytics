//Global utility functions

function $(_id) {
    return document.getElementById(_id);
}

Array.prototype.max = function() {
    var max = this[0];
    var len = this.length;
    for (var i = 1; i < len; i++) if (this[i] > max) max = this[i];
    return max;
}

Array.prototype.min = function() {
    var min = this[0];
    var len = this.length;
    for (var i = 1; i < len; i++) if (this[i] < min) min = this[i];
    return min;
}

wso2vis.util.generateColors = function(count, scheme) {
    function hexNumtoHexStr(n) {
        function toHexStr(N) {
             if (N==null) return "00";
             N=parseInt(N); if (N==0 || isNaN(N)) return "00";
             N=Math.max(0,N); N=Math.min(N,255); N=Math.round(N);
             return "0123456789ABCDEF".charAt((N-N%16)/16)
                  + "0123456789ABCDEF".charAt(N%16);
        }
        
        return "#" + toHexStr((n & 0xFF0000)>>>16) + toHexStr((n & 0x00FF00)>>>8) + toHexStr((n & 0x0000FF));
    }
        
    function generateInterpolatedColorArray(count, colors) {
        function interpolateColors(color1, color2, f) {
            if (f >= 1)
                return color2;
            if (f <= 0)
                return color1;
            var fb = 1 - f;
            return ((((color2 & 0xFF0000) * f)+((color1 & 0xFF0000) * fb)) & 0xFF0000)
                   +((((color2 & 0x00FF00) * f)+((color1 & 0x00FF00) * fb)) & 0x00FF00)
                   +((((color2 & 0x0000FF) * f)+((color1 & 0x0000FF) * fb)) & 0x0000FF);                                   
        }               
        
        var len = colors.length;
        var res = new Array();
        res.push(hexNumtoHexStr(colors[0]));
        
        for (var i = 1; i < count; i++) {
            var val = i * len / count;
            var color1 = Math.floor(val);
            var color2 = Math.ceil(val);
            res.push(hexNumtoHexStr(interpolateColors(colors[color1], colors[color2], val - color1)));                        
        }
        
        return res;
    }

    if (count <= 0) 
        return null;
        
    var a10 = [0x1f77b4, 0xff7f0e, 0x2ca02c, 0xd62728, 0x9467bd,
        0x8c564b, 0xe377c2, 0x7f7f7f, 0xbcbd22, 0x17becf];
    var b20 = [0x1f77b4, 0xaec7e8, 0xff7f0e, 0xffbb78, 0x2ca02c,
          0x98df8a, 0xd62728, 0xff9896, 0x9467bd, 0xc5b0d5,
          0x8c564b, 0xc49c94, 0xe377c2, 0xf7b6d2, 0x7f7f7f,
          0xc7c7c7, 0xbcbd22, 0xdbdb8d, 0x17becf, 0x9edae5];
    var c19 = [0x9c9ede, 0x7375b5, 0x4a5584, 0xcedb9c, 0xb5cf6b,
          0x8ca252, 0x637939, 0xe7cb94, 0xe7ba52, 0xbd9e39,
          0x8c6d31, 0xe7969c, 0xd6616b, 0xad494a, 0x843c39,
          0xde9ed6, 0xce6dbd, 0xa55194, 0x7b4173];
    var colorScheme;
    
    if (scheme == 20) {
        colorScheme = b20;
    }
    else if (scheme == 10) {
        colorScheme = a10;
    }
    else /* any ((scheme === undefined) || (scheme == 19))*/{
        colorScheme = c19;
    }
    
    if (count <= colorScheme.length) {
        c = new Array();
        for (var i = 0; i < count; i++)
            c.push(hexNumtoHexStr(colorScheme[i]));
        return c;
    }
    
    return generateInterpolatedColorArray(count, colorScheme);
}

