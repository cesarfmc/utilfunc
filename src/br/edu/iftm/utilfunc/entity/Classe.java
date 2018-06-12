package br.edu.iftm.utilfunc.entity;

import java.util.ArrayList;
import java.util.List;

public class Classe {
	
	private String name;
	private int id;
	private Classe superClass;
	private String path;
	private List<String> attributes = new ArrayList<String>();
	private List<String> methods = new ArrayList<String>();
	private List<Classe> subClasses = new ArrayList<Classe>();
	
	public Classe(String name, int id, String path){
		this.name = name;
        this.id = id;
        this.path = path;
		this.superClass = null;
	}
	
	public Classe(String name, int id, String path, Classe superClass){
		this.name = name;
        this.id = id;
        this.path = path;
		this.superClass = superClass;
	}
	
	
	
	public String getName(){
		return this.name;
	}
        
    public int getId(){
        return this.id;
    }
	
	public void setSuperClass(Classe classe){
		this.superClass = classe;
	}
	
	public Classe getSuperClass(){
		return this.superClass;
	}
	
	public void addAttribute(String attribute){
		attributes.add(attribute);
	}
	
	public List<String> getAttributes(){
		return attributes;
	}
	
	public void addMethod(String method){
		methods.add(method);
	}
	
	public List<String> getMethods(){
		return methods;
	}
	
	public void addSubClasses(Classe classe){
		subClasses.add(classe);
	}
	
	public List<Classe> getSubClasses(){
		return subClasses;
	}
	
	public String getPath(){
		return path;
	}
}