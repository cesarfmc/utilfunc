package br.edu.iftm.utilfunc.entity;

public class PrototypeExpression {
	
	String classe;
	String membro;
	String tipo;
	
	public PrototypeExpression(String classe, String membro, String tipo) {
		this.classe = classe;
		this.membro = membro;
		this.tipo = tipo;
	}

	public String toString(){
		return classe+".prototype."+membro+" = "+tipo;
	}

	public boolean equals(Object o){
		boolean igual = false;
	    if (o != null && o instanceof PrototypeExpression){
	    	igual = this.classe.equals(((PrototypeExpression) o).classe) && this.membro.equals(((PrototypeExpression) o).membro) && this.tipo.equals(((PrototypeExpression) o).tipo);
	    }
	    return igual;
	}

	public String getClasse() {
		return classe;
	}
	public String getMembro() {
		return membro;
	}
	public String getTipo() {
		return tipo;
	}
}
