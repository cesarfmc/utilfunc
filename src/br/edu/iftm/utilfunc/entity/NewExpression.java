package br.edu.iftm.utilfunc.entity;

public class NewExpression {
	
	String id;

	
	public NewExpression(String id) {
		this.id = id;

	}


	public boolean equals(Object object){
		boolean igual = false;
	    if (object != null && object instanceof NewExpression){
	    	igual = this.id.equals(((NewExpression) object).id);
	    }
	    return igual;
	}
}
