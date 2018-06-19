package br.edu.iftm.utilfunc.entity;

import java.util.ArrayList;
import java.util.List;

public class Function {
	
	private String name;
	private String path;
	private List<String> params = new ArrayList<String>();
	
	public Function(String name, String path){
		this.name = name;
		this.path = path;
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
}