
function desmanchar(funcao) {
       return function() {
             return funcao.call(null, _.toArray(arguments));
       };
}

// 1

function converterIdade(idade) {
 if (!_.isString(idade)) throw new Error("Uma string era esperada");
 var a;
 console.log("Tentativa de converter uma idade ");
 a = parseInt(idade, 10);
 if (_.isNaN(a)) {
   console.log(["Não pode converter a idade:", idade].join(' '));
   a = 0;
 }
 return a;
}

// 2

function isIndexed(dado) {
 return _.isArray(dado) || _.isString(dado);
}

// 3

function nth(a, index) {
 return a[index];
}

// 4

function multiplicar(funcao) {
       return function(array) {
             return funcao.apply(null, array);
       };
}

// 5

function lidarComCSV(str) {
   return _.reduce(str.split("\n"), function(table, row) {
     table.push(_.map(row.split(","), function(c) { return c.trim()}));
     return table;
   }, []);
  };

// 6

function segmentosLetra(n) {
   return _.chain([])
     .push(n + " barris de cerveja na parede")
     .push(n + " barris de cerveja")
     .push("Tome uma, passe pra frente")
     .tap(function(letras) {
       if (n > 1)
         letras.push((n - 1) + " barris de cerveja na parede.");
       else
         letras.push("Sem mais barris de cerveja na parede!");
       })
     .value();
  }

// 7

Utils.fmt = function(str, args) {
    if (!Array.isArray(args)) {
        args = Array.prototype.slice.call(arguments, 1);
    }

    return str.replace(/{([0-9]+)}/g, function(m, i) {
        return args[parseInt(i)];
    });
};

// 8

Utils.changeType = function(value, type) {
    if (value === '' || value === undefined) {
        return undefined;
    }

    switch (type) {
        // @formatter:off
        case 'integer':
            if (typeof value === 'string' && !/^-?\d+$/.test(value)) {
                return value;
            }
            return parseInt(value);
        case 'double':
            if (typeof value === 'string' && !/^-?\d+\.?\d*$/.test(value)) {
                return value;
            }
            return parseFloat(value);
        case 'boolean':
            if (typeof value === 'string' && !/^(0|1|true|false){1}$/i.test(value)) {
                return value;
            }
            return value === true || value === 1 || value.toLowerCase() === 'true' || value === '1';
        default: return value;
        // @formatter:on
    }
};

// 9

Utils.escapeString = function(value) {
    if (typeof value != 'string') {
        return value;
    }

    return value
        .replace(/[\0\n\r\b\\\'\"]/g, function(s) {
            switch (s) {
                // @formatter:off
                case '\0': return '\\0';
                case '\n': return '\\n';
                case '\r': return '\\r';
                case '\b': return '\\b';
                default:   return '\\' + s;
                // @formatter:off
            }
        })
        // uglify compliant
        .replace(/\t/g, '\\t')
        .replace(/\x1a/g, '\\Z');
};

// 10

Utils.escapeRegExp = function(str) {
    return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, '\\$&');
};

// 11

Utils.escapeElementId = function(str) {
    // Regex based on that suggested by:
    // https://learn.jquery.com/using-jquery-core/faq/how-do-i-select-an-element-by-an-id-that-has-characters-used-in-css-notation/
    // - escapes : . [ ] ,
    // - avoids escaping already escaped values
    return (str) ? str.replace(/(\\)?([:.\[\],])/g,
            function( $0, $1, $2 ) { return $1 ? $0 : '\\' + $2; }) : str;
};

// 12

Utils.groupSort = function(items, key) {
    var optgroups = [];
    var newItems = [];

    items.forEach(function(item) {
        var idx;

        if (item[key]) {
            idx = optgroups.lastIndexOf(item[key]);

            if (idx == -1) {
                idx = optgroups.length;
            }
            else {
                idx++;
            }
        }
        else {
            idx = optgroups.length;
        }

        optgroups.splice(idx, 0, item[key]);
        newItems.splice(idx, 0, item);
    });

    return newItems;
};


(function(x, y) {
	var f = function(){ //utilitária porque atende todos os critérios
		console.log("teste");
	}
	f();
	console.log(x + y);
})(5, 5); 



var foo = function(x, y) {
	var bar = function(){ //não é utilitária porque esta aninhada a um funcao com nome
		console.log("teste");
	}
	bar();
	console.log(x + y);
}; 

// 13



// 3 funções não foram classificadas do arquivo util.js

// 3 funções não foram classificadas do arquivo utilDevMedia.js


/*

var listener = (function() {
	function listenerAdd(elm, evt, func) {
		if( elm.addEventListener ) {
			elm.addEventListener(evt, func, false);
		} else if( elm.attachEvent ) {
			elm.attachEvent('on'+evt, func);
		}
	};
	function listenerRemove(elm, evt, func) {
		if( elm.removeEventListener ) {
			elm.removeEventListener(evt, func, false);
		} else if( elm.detachEvent ) {
			elm.detachEvent('on'+evt, func);
		}
	};
	return {
		add: listenerAdd,
		remove: listenerRemove
	}
}());


*/


//colocar 10 novas funções utilitarias

(function() {
	  function removeDuplicates(a) {
	    return a.filter(function(e, pos) {
	      return a.indexOf(e) == pos;
	    });
	  }
	  
	  function reverseString(s) {
		    return s.split('').reverse().join('');
		  }
	  
	  // Problemas
	  
})
	  
	  function reverseString(s) {
	    return s.split('').reverse().join('');
	  }
	  
	  function arraysEqual(a, b) {
	    if (a === b) { return true };
	    if (a == null || b == null) { return false };
	    if (a.length != b.length) { return false };

	    for (var i = 0; i < a.length; ++i) {
	      if (a[i] !== b[i]) { return false };
	    }
	    return true;
	  }
	  
	  function dec2bin(dec) {
	    return baseConvert(dec, 10, 2); // parseInt(dec, 10).toString(2)
	  }
	  
	  function bin2dec(bin) {
	    return baseConvert(bin, 2, 10); // parseInt(bin, 2)
	  }
	  
	  function dec2hex(dec) {
	    return baseConvert(dec, 10, 16); // parseInt(dec, 10).toString(16)
	  }
	  
	  function hex2dec(hex) {
	    return baseConvert(hex, 16, 10); // parseInt(hex, 16)
	  }
	  
	  function baseConvert(num, b1, b2) {
	    return parseInt(num, b1).toString(b2);
	  }
	  
	  function ascii2bin(ascii) {
	    if (ascii.length == 0) return;
	    var bin = '';
	    for (var i = 0; i < ascii.length; i++) {
	      bin += ('00000000' + ascii.charCodeAt(i).toString(2)).slice(-8);
	    }
	    return bin;
	  }
	  
	  function bin2ascii(bin) {
	    var bin = bin.match(/[01]{8}/g);
	    if (bin.length == 0) return;
	    var ascii = '';
	    for (var i = 0; i < bin.length; i++) {
	      ascii += String.fromCharCode(parseInt(bin[i],2));
	    }
	    return ascii;
	  }
	  
	  function ascii2hex(ascii){
	    if (ascii.length == 0) return;
	    var hex = '';
	    for (var i = 0; i < ascii.length; i++) {
	      hex += ('0000' + ascii.charCodeAt(i).toString(16)).slice(-4);
	    }
	    return hex;
	  }
	  
	  function hex2ascii(hex) {
	    hex = hex.match(/[0-9A-Fa-f]{4}/g);
	    if (hex.length == 0) return;
	    var ascii = '';
	    for (var i = 0; i < hex.length; i++) {
	      ascii += String.fromCharCode(parseInt(hex[i],16));
	    }
	    return ascii;
	  }
	  
	  function ascii2base(dec) {
	    return btoa(dec);
	  }
	  
	  function base2ascii(num) {
	    return atob(num);
	  }

	  function test(func, expect) {
	    var input = Array.prototype.slice.call(arguments).slice(2);
	    var output = func.apply(func, input);
	    var funcName = /^function\s+([\w\$]+)\s*\(/.exec(func.toString())[1];
	    var equal = expect === output || arraysEqual(expect, output);
	    console[equal ? 'log' : 'error'](funcName, '\n    input: ', input, '\n    output:', output, '\n    expect:', expect);
	    document.getElementById('output').innerHTML +=
	      (equal ? '✔' : '✘') + ' ' + funcName + '\n    input:  ' + input + '\n    output: ' + output + '\n    expect: ' + expect + '\n';
	  }

//colocar 10 novas funções não utilitárias
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  

