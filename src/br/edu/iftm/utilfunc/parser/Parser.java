package br.edu.iftm.utilfunc.parser;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.script.ScriptException;

import au.com.bytecode.opencsv.CSVWriter;
import br.edu.iftm.utilfunc.entity.Function;
import rinoceronte.Esprima;

public class Parser {

	private String dirPath;
	private List<Function> functions = new ArrayList<Function>();
	private ArrayList<JsonValue> change;
	private boolean util = true;
	private String file;
	
	public Parser(String dirPath) {
		this.dirPath = dirPath;
		this.change = new ArrayList<>();
	}

	public void parse() throws NoSuchMethodException, ScriptException, IOException {
		File diretorioRaiz = new File(dirPath);
		ArrayList<File> diretorios = new ArrayList<File>();
		diretorios.add(diretorioRaiz);
		for (File dir : diretorios) {
			if (dir.isDirectory()) {
				List<File> files = listf(dir.getAbsolutePath());
				List<File> filesJSON = generateJSON(files);
				generateUtilFunctions(filesJSON);
			}
		}
		printCSV();
	}

	private void generateUtilFunctions(List<File> filesJSON) throws FileNotFoundException  {
		
		for (File file : filesJSON) {
			this.file = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-2);
			JsonReader jsonReader = Json.createReader(new FileReader(file));
			JsonObject jsonObject = jsonReader.readObject();
			JsonArray ArrayBoby = jsonObject.getJsonArray("body");
			for(JsonValue obj : ArrayBoby) {
				JsonObject object = (JsonObject) obj;
				breakInPieces(object);
			}
		}
	}
	
	private void breakInPieces(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if(entry.getValue().toString().equals("\"VariableDeclaration\""))  {
					if(isFunctionOnVariable(jsonObject)) {
						   checkFunctionOnVariable(jsonObject);
							if(util) {
								buildListUtilityFunction();	
								change.clear();
							}
							util = true;
					}
				}else if(entry.getValue().toString().equals("\"FunctionDeclaration\"")) {
					checkFunction(jsonObject);
					if(util) {
						buildListUtilityFunction();
						change.clear();
					}
					util = true;
				}
			} else if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
					
				}else {
					JsonArray array = (JsonArray) entry.getValue();
					JsonObject object = null;
					for(JsonValue obj : array) {
					  object = (JsonObject) obj;
					  breakInPieces(object);
					}
				}	
			} else if (entry.getValue() instanceof JsonObject) {
					JsonObject object = (JsonObject) entry.getValue();
					breakInPieces(object);
			}
		}
	}
    
	
	
	private void checkFunction(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if(entry.getKey().equals("name")) {
					if((entry.getValue().toString().equals("\"get\"")) || (entry.getValue().toString().equals("\"set\""))) {
						util = false;
					}
				}else if(entry.getValue().toString().equals("\"ReturnStatement\"")) {
					 util = checkReturn(jsonObject);
				}
			} else if (entry.getValue() instanceof JsonArray) {
				
				if(entry.getKey().equals("params")) {
				   change.add(entry.getValue());
				}else if(entry.getKey().equals("range")) {
					
				}else if(entry.getKey().equals("body")) {
					JsonArray array = (JsonArray) entry.getValue();
					JsonObject object = null;
                    if(array.isEmpty()) {
                    	util = false;
                    }else {
                    	for(JsonValue obj : array) {
      					  object = (JsonObject) obj;
      					  if(isFunctionDeclaration(object)) {
      						util = false;
      					  }else if(isFunctionOnVariable(object)) {
      						  util = false;
      					  }else {
          				   checkFunction(object);
      					  }
      					}
                    }
				}else {
					JsonArray array = (JsonArray) entry.getValue();
					JsonObject object = null;
					for(JsonValue obj : array) {
					  object = (JsonObject) obj;
					   checkFunction(object);
					}	
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if(entry.getKey().equals("id")) {
					change.add(entry.getValue());
				}else {
					JsonObject object = (JsonObject) entry.getValue();
					 checkFunction(object);	
				}
			}else if((entry.getKey().equals("id"))) {
				if(change.isEmpty()) {
					System.out.println("Não é utilitária");
				}
			}
		}
	}
	
	private boolean checkFunctionOnVariable(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						return  checkFunctionOnVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {

				if((entry.getKey().equals("id"))) {
					
					change.add(entry.getValue());
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					return  checkFunctionOnVariable(obj1);	
				}
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					checkFunction(jsonObject);
				}
			}
		}
		return false;
	}
	private boolean isFunctionDeclaration(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						return  isFunctionDeclaration(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {

				if(entry.getKey().toString().equals("id")) {
					
					change.add(entry.getValue());
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					return  isFunctionDeclaration(obj1);	
				}
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionDeclaration\"")) {
					return false;
				}
			}
		}
		return false;
	}
	private boolean isFunctionOnVariable(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						return  isFunctionOnVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
					JsonObject obj1 = (JsonObject) entry.getValue();
					if(entry.getKey().equals("id")) {
						
					}else {
						return  isFunctionOnVariable(obj1);		
					}
				
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	private boolean checkReturn(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						return  checkReturn(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
					JsonObject obj1 = (JsonObject) entry.getValue();
					return  checkReturn(obj1);	
				
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"null\"")) {
					return false;
				}else if(entry.getValue().toString().equals("\"true\"")) {
					return false;
				}else if(entry.getValue().toString().equals("\"false\"")) {
					return false;
				}
			}
		}
		return true;
	}
	
	private void buildListUtilityFunction() {
	
		String funcName;
		JsonObject object = (JsonObject) change.get(0);
		JsonArray array= (JsonArray) change.get(1);
		funcName = object.getJsonString("name").toString().replaceAll("\"", "");
		Function function = new Function(funcName,file);
		
		for (JsonValue obj : array) {
			object = (JsonObject) obj;
			function.addParam(object.getJsonString("name").toString().replaceAll("\"", ""));
		}
		functions.add(function);
		
	}
	
	private List<File> generateJSON(List<File> files) throws NoSuchMethodException, ScriptException, IOException {
		ArrayList<File> filesJSON = new ArrayList<File>();
		for (File file : files) {
			if (file.isFile() && file.getPath().endsWith(".js")) {
				File arquivoJS = file;
				File arquivoJSON = new File(file.getPath().substring(0, file.getPath().length() - 3) + ".json");
				if (!arquivoJSON.exists()) {
					Esprima esprima = new Esprima(arquivoJS, arquivoJSON);
					esprima.parse();
				}
				filesJSON.add(arquivoJSON);
			}
		}
		return filesJSON;
	}

	private List<File> listf(String directoryName) {
		File directory = new File(directoryName);
		List<File> resultList = new ArrayList<File>();
		File[] fList = directory.listFiles();
		resultList.addAll(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isFile()) {
			} else if (file.isDirectory()) {
				resultList.addAll(listf(file.getAbsolutePath()));
			}
		}
		return resultList;
	}

	private void printCSV() throws IOException {
		String[] args = dirPath.split("/");
		String fileName = args[args.length-1]+".csv";
		List<String[]> data = new ArrayList<String[]>();
		for (Function function : functions) {
			System.out.format("\n%s;%s;", function.getPath(), function.getName());
			for(String param : function.getParams()){
				System.out.format("%s;", param);
			}
			String [] params = function.getParams().toArray(new String[0]);
			String[] nameAndPath = {function.getPath(), function.getName()};
			data.add(concat(nameAndPath, params));
		}
		CSVWriter writer = new CSVWriter(new FileWriter(fileName), ';');
		writer.writeAll(data);
		writer.close();
	}
	
	String[] concat(String[] first, String[] second) {
	    List<String> both = new ArrayList<String>(first.length + second.length);
	    Collections.addAll(both, first);
	    Collections.addAll(both, second);
	    return both.toArray(new String[both.size()]);
	}


}
