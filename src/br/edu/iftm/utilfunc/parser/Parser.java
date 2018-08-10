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
	private ArrayList<JsonValue> idFunction;
	private ArrayList<String> localVariables;
	private ArrayList<String> globalVariables;
	private boolean util = true;
	private String file;

	public Parser(String dirPath) {
		this.dirPath = dirPath;
		this.idFunction = new ArrayList<>();
		this.localVariables = new ArrayList<>();
		this.globalVariables = new ArrayList<>();
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
				globalVariables.clear();
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
				lookForGlobalVariables(object);
			}
			for(JsonValue obj : ArrayBoby) {
				JsonObject object = (JsonObject) obj;
				breakInPieces(object);
			}
		}
	}

	private void lookForGlobalVariables(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if(entry.getValue().toString().equals("\"VariableDeclaration\""))  {
					getGlobalVariable(jsonObject);
				}else if(entry.getValue().toString().equals("\"ExpressionStatement\"")) {
					getGlobalVariableOnExpression(jsonObject);	
				}
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
						}
						idFunction.clear();
						localVariables.clear();
						util = true;
					}

				}else if(entry.getValue().toString().equals("\"FunctionDeclaration\"")) {
					checkFunction(jsonObject);
					if(util) {
						buildListUtilityFunction();
					}
					idFunction.clear();
					localVariables.clear();
					util = true;
				}
			} else if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {

				}else {
					JsonArray array = (JsonArray) entry.getValue();
					JsonObject object = null;
					for(JsonValue obj : array) {
						if(obj instanceof JsonObject) {
							object = (JsonObject) obj;
							breakInPieces(object);
						}
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
				if(entry.getValue().toString().equals("\"ReturnStatement\"")) {
					util = checkReturn(jsonObject);
				}else if(entry.getValue().toString().equals("\"ThisExpression\"")) {
					util = false;
					return;
				}
			} else if (entry.getValue() instanceof JsonArray) {
				JsonArray array;
				if(entry.getKey().equals("params")) {
					array = (JsonArray) entry.getValue();
					idFunction.add(entry.getValue());
					getVariableNameOnParams(array);
				}else if(entry.getKey().equals("range")) {
				}else if(entry.getKey().equals("body")) {
					array = (JsonArray) entry.getValue();
					JsonObject object = null;
					if(array.isEmpty()) {
						util = false;
						return;
					}else {
						for(JsonValue obj : array) {
							object = (JsonObject) obj;
							checkFunctionDeclaration(object); 
							if(!util) {
								return;
							}else {
								verifyFunctionOnVariable(object);
								if(!util) {
									return;
								}else {
									searchVariable(object);
								}
							}
						}
						for(JsonValue obj : array) {
							object = (JsonObject) obj;
							isDOMwithLiteralOnArgument(object);
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
						}else {
							util = false;
							return;
						}
					}
				}else {
					JsonObject object = (JsonObject) entry.getValue();
					checkFunction(object);	
				}
			}else if((entry.getKey().equals("id"))) {
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
					JsonObject idObject = (JsonObject) entry.getValue();	
					if(idFunction.isEmpty()) {
						if(checkFunctionName(idObject)) {
							idFunction.add(idObject);	
						}else {
							util = false;
							return false;
						}	
					}
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
	private void checkFunctionDeclaration(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						checkFunctionDeclaration(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {

				if(entry.getKey().toString().equals("id")) {
					JsonObject idObject = (JsonObject) entry.getValue();	
					if(idFunction.isEmpty()) {
						if(checkFunctionName(idObject)) {
							idFunction.add(idObject);	
						}else {
							util = false;
							return;
						}	
					}
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					checkFunctionDeclaration(obj1);	
				}
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionDeclaration\"")) {
					util = false;
					return;
				}else if(entry.getValue().toString().equals("\"FunctionExpression\"")) {
					util = false;
					return;
				}
			}
		}
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
				return  isFunctionOnVariable(obj1);		
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					return true;
				}
			}
		}
		return false;
	}


	private void verifyFunctionOnVariable(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						verifyFunctionOnVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				verifyFunctionOnVariable(obj1);		
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"FunctionExpression\"")) {
					util = false;
					return;
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

	private void isDOMwithLiteralOnArgument(JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().toString().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						isDOMwithLiteralOnArgument(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				isDOMwithLiteralOnArgument(obj1);	
			} else if (entry.getValue() instanceof JsonString) {

				if (entry.getValue().toString().equals("\"$\"")) {
					idFunction.add(entry.getValue());
				}else if (entry.getValue().toString().equals("\"document\"")) {
					idFunction.add(entry.getValue());
				}else if(entry.getValue().toString().equals("\"getElementById\"")) {
					idFunction.add(entry.getValue());
				}else if(entry.getValue().toString().equals("\"Literal\"")) {
					if(idFunction.get(idFunction.size() - 1).toString().equals("\"getElementById\"")) {
						if(idFunction.get(idFunction.size() - 2).toString().equals("\"document\"")) {
							idFunction.remove(idFunction.size() - 1);
							idFunction.remove(idFunction.size() - 1);
							util = false;
						}
					}else {
						if(idFunction.get(idFunction.size() - 1).toString().equals("\"$\"")) {
							idFunction.remove(idFunction.size() - 1);
							util = false;
						}
					}
				}
			}
		}
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
						object = (JsonObject) obj;
						searchVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				searchVariable(obj1);	

			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equalsIgnoreCase("\"VariableDeclaration\"")) {
					getLocalVariable(jsonObject);
				}else if(entry.getValue().toString().equalsIgnoreCase("\"ExpressionStatement\"")) {
					getLocalVariableOnExpression(jsonObject);
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
					getLocalVariableName(jsonObject);
					return;
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					getLocalVariable(obj1);	
				}
			}
		}
	}


	private void getGlobalVariable (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else if(entry.getKey().equals("declarations")) {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						getGlobalIdObject(object);
					}	
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						getGlobalVariable(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				JsonObject obj1 = (JsonObject) entry.getValue();
				getGlobalVariable(obj1);	

			}
		}
	}


	private void getGlobalIdObject (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getKey().equals("id")) {
				JsonObject property = (JsonObject) entry.getValue();
				getGlobalVariableName(property);
			}
		}
	}


	private boolean getGlobalVariableOnExpression (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						return getGlobalVariableOnExpression(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if(entry.getKey().equals("left")) {
					JsonObject obj = (JsonObject) entry.getValue();
					return getGlobalVariableInLeft(obj);
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					return getGlobalVariableOnExpression(obj1);	
				}
			} 
		}
		return false;
	}


	private void getGlobalVariableName (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getKey().equals("name")) {
					globalVariables.add(entry.getValue().toString());
				}
			}
		}
	}



	private boolean getLocalVariableOnExpression (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonArray) {
				if(entry.getKey().equals("range")) {
				}else {
					JsonObject object = null;
					JsonArray array = (JsonArray) entry.getValue();
					for (JsonValue obj : array) {
						object = (JsonObject) obj;
						return getLocalVariableOnExpression(object);
					}
				}
			} else if (entry.getValue() instanceof JsonObject) {
				if(entry.getKey().equals("left")) {
					getLocalVariableInLeft(jsonObject);
				}else {
					JsonObject obj1 = (JsonObject) entry.getValue();
					return getLocalVariableOnExpression(obj1);	
				}
			} 
		}
		return false;
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

	private boolean getGlobalVariableInLeft (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equalsIgnoreCase("\"Identifier\"")) {
					getGlobalVariableName(jsonObject);
				}else {
					getGlobalPropertyObject(jsonObject);
				}
			}
		}
		return false;
	}

	private boolean getLocalVariableInLeft (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equalsIgnoreCase("\"Identifier\"")) {
					compareVariableName(jsonObject);
				}else {
					getLocalPropertyObject(jsonObject);
				}
			}
		}
		return false;
	}


	private void getGlobalPropertyObject (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getKey().equals("property")) {
				JsonObject property = (JsonObject) entry.getValue();
				getGlobalVariableName(property);
			}
		}
	}

	private void getLocalPropertyObject (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getKey().equals("property")) {
				JsonObject property = (JsonObject) entry.getValue();
				compareVariableName(property);
			}
		}
	}



	private void compareVariableName (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getKey().equals("name")) {
					String name= entry.getValue().toString();
					for(String global : globalVariables) {
						if(global.equals(name)) {
							for(String local : localVariables) {
								if(local.equals(name)) {
									return ;
								}
							}
							util = false;
							return;
						}
					}

					for (String local : localVariables) {
						if(local.equals(name)) {
							return;
						}
					}
					localVariables.add(name);
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
				JsonObject obj1 = (JsonObject) entry.getValue();
				compare(obj1);		
			} else if (entry.getValue() instanceof JsonString) {
				if (entry.getValue().toString().equals("\"Identifier\"")) {
					checkVariableNameOnEscope(jsonObject);
				}
			}
		}
	}

	private void checkVariableNameOnEscope (JsonObject jsonObject) {

		Set<Entry<String, JsonValue>> myset = jsonObject.entrySet();
		for (Entry<String, JsonValue> entry : myset) {
			if (entry.getValue() instanceof JsonString) {
				if (entry.getKey().equals("name")) {
					String name= entry.getValue().toString();
					for (String local : localVariables) {
						if(local.equals(name)) {
							return;
						}
					}
					for(String global : globalVariables) {
						if(global.equals(name)) {
							for(String local : localVariables) {
								if(local.equals(name)) {
									return ;
								}
							}
							util = false;
							return;
						}
					}


				}
			}
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

	private void buildListUtilityFunction() {

		String funcName;
		JsonObject object = (JsonObject) idFunction.get(0);
		JsonArray array= (JsonArray) idFunction.get(1);
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
