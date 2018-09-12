
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

//colocar 10 novas funções não utilitárias

