package br.edu.iftm.utilfunc.main;

import br.edu.iftm.utilfunc.parser.Parser;

public class Main {

    public static void main(String[] args) throws Exception {
    	Parser p = new Parser(args[0]);
    	p.parse();
    }
}
