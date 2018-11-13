package br.edu.iftm.utilfuncplugin.entity;


public class Function {
    private String path;
    private String func;
    private String params;
    
    
	public Function() {
		super();
		
	}
	public Function(String path, String func, String params) {
		super();
		this.path = path;
		this.func = func;
		this.params = params;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getFunc() {
		return func;
	}
	public void setFunc(String func) {
		this.func = func;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
   
    
}