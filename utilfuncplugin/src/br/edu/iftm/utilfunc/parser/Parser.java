package br.edu.iftm.utilfunc.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import br.edu.iftm.utilfunc.entity.Function;
import rinoceronte.Esprima;

public class Parser {

	private String dirPath;
	private String csvFilePath;
	private List<Function> functions = new ArrayList<Function>();
	private ArrayList<JsonValue> idFunction;
	private ArrayList<String> localVariables;
	private boolean util = true,check,test;
	private String file,expName,line;

	public Parser(String dirPath) throws Exception{
		this.dirPath = dirPath;
		Bundle bundle = Platform.getBundle("utilfuncplugin");
		URL eclipseURL = FileLocator.find(bundle, new Path("HostVariable.csv"), null);
        URL fileURL = FileLocator.toFileURL(eclipseURL);
		this.csvFilePath = fileURL.getPath();
		this.idFunction = new ArrayList<>();
		this.localVariables = new ArrayList<>();
	}

	public void parse() throws IOException, NoSuchMethodException, ScriptException  {
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
		//printCSV();
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

	private void breakInPieces(JsonObject jsonObject)  {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if(entry.getValue().toString().equals("\"VariableDeclaration\""))  {
					FunctionOnVariable(jsonObject);
					if(!util) {
						util = true;
						loadCsvFile();
						checkFunctionOnVariable(jsonObject);
						if(util) {
							buildListUtilityFunction();	
						}
						idFunction.clear();
						localVariables.clear();
						util = true;
					}
				}else if(entry.getValue().toString().equals("\"FunctionDeclaration\"")) {
					loadCsvFile();
					checkFunction(jsonObject);
					if(util) {
						buildListUtilityFunction();
					}
					idFunction.clear();
					localVariables.clear();
					util = true;
				}else if(entry.getValue().toString().equals("\"ExpressionStatement\"")) {
					FunctionExpressionOnExpression(jsonObject);
					if(!util) {
							util = true;
						loadCsvFile();
						checkFunctionOnExpression(jsonObject);
						if(util) {
							buildListUtilityFunction();
						}
						idFunction.clear();
						localVariables.clear();
						util = true;
					}
				}
				
				break;
			} 
		}
	}

	private void checkFunction(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if(entry.getValue().toString().equals("\"ReturnStatement\"")) {
					util = checkReturn(jsonObject);
				}else if(entry.getValue().toString().equals("\"ThisExpression\"")) {
					util = false;
					return;
				}
			} else if (entry.getValue() instanceof JsonArray) {
				JsonArray array;
				if(entry.getKey().equals("params")) {
					if(idFunction.isEmpty()) {
						util = false;
						idFunction.add(entry.getValue());
					}else {
						array = (JsonArray) entry.getValue();
						idFunction.add(entry.getValue());
						getVariableNameOnParams(array);
					}
				}else if(entry.getKey().equals("range")) {
				}else if(entry.getKey().equals("body")) {
					boolean status;
					array = (JsonArray) entry.getValue();
					JsonObject object = null;
					if(array.isEmpty()) {
						util = false;
						return;
					}else {
						for(JsonValue obj : array) {
							object = (JsonObject) obj;
							if(!util) {
								status = util;
								startOver(object);
								util = status;
							}else {
								searchVariable(object);
							}
						}
						for(JsonValue obj : array) {
							object = (JsonObject) obj;
							checkJquery(object);
							if(!util) {
								return;
							}else {	
								compare(object);
								if(!util) {
									return;	
								}else {
									checkFunction(object);	
								}
							}
						}
					}
				}else {
					array = (JsonArray) entry.getValue();
					JsonObject object = null;
					for(JsonValue obj : array) {
						object = (JsonObject) obj;
						checkFunction(object);
					}	
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if(entry.getKey().equals("id")) {
					JsonObject idObject = (JsonObject) entry.getValue();	
					if(idFunction.isEmpty()) {
						if(checkFunctionName(idObject)) {
							idFunction.add(entry.getValue());
							getLocalVariableName(idObject);
						}else {
							util = false;
							return;
						}
					}
				}else if(entry.getKey().equals("loc")) {
					JsonObject object = (JsonObject) entry.getValue();
					getLocation(object);
				}else{
					JsonObject object = (JsonObject) entry.getValue();
					checkFunction(object);	
				}
			}else if((entry.getKey().equals("id"))) {
			}
		}
	}

	private void getLocation(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			 if (entry.getValue() instanceof JsonObject) {
				 JsonObject obj1 = (JsonObject) entry.getValue();
				 if((entry.getKey().equals("start"))) {
					 startLine(obj1);
				 }else {
				  getLocation(obj1);	
				 }
			} else if (entry.getValue() instanceof JsonNumber) {
				if (entry.getKey().equals("line")) {
					line = line +" - "+entry.getValue().toString();
				}
			}
		}
	}
	
	private void startLine(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
		      if (entry.getValue() instanceof JsonNumber) {
				if (entry.getKey().equals("line")) {
					line = entry.getValue().toString();
				}
			}
		}
	}
	
	private void checkFunctionOnVariable(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						checkFunctionOnVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if((entry.getKey().equals("id"))) {
					JsonObject idObject = (JsonObject) entry.getValue();	
					if(idFunction.isEmpty()) {
						if(checkFunctionName(idObject)) {
							idFunction.add(idObject);
							getLocalVariableName(idObject);
						}else {
							util = false;
						}	
					}
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					checkFunctionOnVariable(obj1);	
				}
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					checkFunction(jsonObject);
				}
			}
		}
	}

	private void checkFunctionOnExpression(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						if(obj instanceof JsonObject) {
							object = (JsonObject) obj;
							checkFunctionOnExpression(object);
						}
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if((entry.getKey().equals("left"))) {
					JsonObject left = (JsonObject) entry.getValue();
					checkLeft(left);
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					checkFunctionOnExpression(obj1);	
				}
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					checkFunction(jsonObject);
				}
			}
		}
	}

	private void checkLeft(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"Identifier\"")) {
					idFunction.add(jsonObject);
					return;
				}else {
					expName = "";
					buildNameFunction(jsonObject);
					if(!expName.isEmpty()) {
						expName = expName.substring(1, expName.length() - 1);
						expName = expName.replaceAll("\"", "");
						JsonObjectBuilder name = Json.createObjectBuilder();
						name = name.add("type", "Identifier");
						name = name.add("name", expName);
						idFunction.add(name.build());
						return;
					}
				}
			}
		}
	}

	private void buildNameFunction(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						buildNameFunction(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				buildNameFunction(obj1);	
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getKey().toString().equals("name")) {
					String name = entry.getValue().toString()+".";
					expName = expName.concat(name);
				}
			}
		}
	}

	private void FunctionExpressionOnExpression(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						if(obj instanceof JsonObject) {
							object = (JsonObject) obj;
							FunctionExpressionOnExpression(object);
						}
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				FunctionExpressionOnExpression(obj1);	

			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					util = false;
				}	
			}
		}
	}

	private void FunctionOnVariable(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						FunctionOnVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				FunctionOnVariable(obj1);		
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					util = false;
				}
			}
		}

	}

	private boolean checkFunctionName (JsonObject jsonObject) {
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
				String name = entry.getValue().toString();
				try {
					name = name.substring(1,4);
					if (name.equalsIgnoreCase("get")) {
						return false;
					}else if(name.equalsIgnoreCase("set")) {
						return false;
					}
				}catch(Exception e) {
				}
			}
		}
		return true;
	}

	private void checkJquery(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else if((entry.getKey().toString().equals("arguments")) && (check) && (test)) {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						checkArguments(object);
					}
				}else if((entry.getKey().toString().equals("arguments")) && (check)) {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						checkArguments(object);
					}
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						checkJquery(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				checkJquery(obj1);	
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"$\"")) {
			      check = true;
				}else if (entry.getValue().toString().equals("\"document\"")) {
				      check = true;
				}else  if (entry.getValue().toString().equals("\"getElementById\"")) {
				      test = true;
				}
			}
		}
	}

	private void checkArguments(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						checkArguments(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				checkArguments(obj1);	
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"Literal\"")) {
			          util = false;
				}
			}
		}
	   check = false;
	   test = false;
	}

	private void getVariableNameOnParams (JsonArray myArray) {

		JsonObject object = null;
		for (JsonValue obj : myArray) {
			object = (JsonObject) obj;
			Set<Entry<String, JsonValue>> myset = object.entrySet();
			for (Entry<String, JsonValue> entry : myset) {
				if (entry.getValue() instanceof JsonString) {
					if (entry.getKey().equals("name")) {
						localVariables.add(entry.getValue().toString());
					}
				}
			}
		}
	}

	private void searchVariable (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						if (obj instanceof JsonObject) {
							object = (JsonObject) obj;
							searchVariable(object);
						}
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				searchVariable(obj1);	
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"VariableDeclaration\"")) {
					getLocalVariable(jsonObject);
				}else if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					checkFunction(jsonObject);
				}	
			}
		}
	}

	private void getLocalVariable (JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						getLocalVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if(entry.getKey().equals("id")) {
					JsonObject objId = (JsonObject) entry.getValue();
					getLocalVariableName(objId);
					return;
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					getLocalVariable(obj1);	
				}
			}
		}
	}

	private void getLocalVariableName (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getKey().equals("name")) {
					localVariables.add(entry.getValue().toString());
				}
			}
		}
	}

	private void compare(JsonObject jsonObject) {
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						compare(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if(entry.getKey().equals("property")) {	
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					compare(obj1);	
				}		
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"Identifier\"")) {
					checkVariableNameOnEscope(jsonObject);
				}
			}
		}
	}

	private void checkVariableNameOnEscope (JsonObject jsonObject)  {
		String name;
		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getKey().equals("name")) {
					name= entry.getValue().toString();
					for (String local : localVariables) {
						if(local.equals(name)) {
							return;
						}
					}
					checkIfNative(name);
					return;
				}
			}
		}
	}

	private void startOver(JsonObject jsonObject) {
		idFunction.clear();
		localVariables.clear();
		util = true;
		breakInPieces(jsonObject);
	}
	
	
	
	
	private void checkIfNative(String name) {
		name = name.replaceAll("\"", "");
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		ScriptContext context = engine.getContext();
		try {
			engine.eval(name,context);
		} catch (ScriptException e) {
			e.getMessage();
			util = false;
		}
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

	private void loadCsvFile() {
		String nextLine[],name;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFilePath),';');
			while ((nextLine = reader.readNext()) != null) {
				name = nextLine[0];
				name = "\""+name+"\"";
				localVariables.add(name); 
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildListUtilityFunction() {
		String funcName;
		try {
			JsonObject object = (JsonObject) idFunction.get(0);
			JsonArray array= (JsonArray) idFunction.get(1);
			funcName = object.getJsonString("name").toString().replaceAll("\"", "");
			Function function = new Function(funcName,file,line);
			for (JsonValue obj : array) {
				object = (JsonObject) obj;
				function.addParam(object.getJsonString("name").toString().replaceAll("\"", ""));
			}
			functions.add(function);
		}catch(Exception e) {	
	
		}
	}

	private List<File> generateJSON(List<File> files) throws NoSuchMethodException, ScriptException, IOException {
		ArrayList<File> filesJSON = new ArrayList<File>();
		for (File file : files) {
			if (file.isFile() && file.getPath().endsWith(".js")) {
				File arquivoJS = file;
				File arquivoJSON = new File(file.getPath().substring(0, file.getPath().length() - 3) + ".json");
				//if (!arquivoJSON.exists()) {
					Esprima esprima = new Esprima(arquivoJS, arquivoJSON);
					esprima.parse();
				//}
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

	public List<Function> getFunctions() {
		return functions;
	}
	
	
}
