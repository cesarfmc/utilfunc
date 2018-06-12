package br.edu.iftm.utilfunc.entity;

public class InheritanceExpression {
	
	String classePai;
	String classeFilha;
	
	public InheritanceExpression(String classePai, String classeFilha) {
		this.classePai = classePai;
		this.classeFilha = classeFilha;

	}

	public String toString(){
		return classeFilha+".prototype = new "+classePai+"();";
	}

	public boolean equals(Object o){
		boolean igual = false;
	    if (o != null && o instanceof InheritanceExpression){
	    	igual = this.classePai.equals(((InheritanceExpression) o).classePai) && this.classeFilha.equals(((InheritanceExpression) o).classeFilha);
	    }
	    return igual;
	}

	public String getClassePai() {
		return classePai;
	}
	public String getClasseFilha() {
		return classeFilha;
	}
}
