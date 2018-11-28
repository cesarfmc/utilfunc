package br.edu.iftm.utilfuncplugin.entity;


public class Function {
    private String path;
    private String func;
    private String params;
    private String line;
    
    
	public Function() {
		super();
		
	}
	public Function(String path, String func, String params, String line) {
		super();
		this.path = path;
		this.func = func;
		this.params = params;
		this.line = line;
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
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
}