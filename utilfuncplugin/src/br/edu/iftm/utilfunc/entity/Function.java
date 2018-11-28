package br.edu.iftm.utilfunc.entity;

import java.util.ArrayList;
import java.util.List;

public class Function {
	
	private String name;
	private String path;
	private String line;
	private List<String> params = new ArrayList<String>();
	
	public Function(String name, String path, String line){
		this.name = name;
		this.path = path;
		this.line = line;
	}
	
	public String getName(){
		return this.name;
	}
    
	public void addParam(String param){
		params.add(param);
	}
	
	public List<String> getParams(){
		return params;
	}
	
	public String getPath(){
		return path;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}
	
}