package rinoceronte;

import static javax.script.ScriptContext.ENGINE_SCOPE;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class Esprima {
    private File arquivoJS;
    private File arquivoJSON;
   
	public Esprima(File arquivoJS, File arquivoJSON){
		this.arquivoJS = arquivoJS;
		this.arquivoJSON = arquivoJSON;
	}
	
    static String readFile(String fileName) throws IOException,FileNotFoundException {
        return new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
    }    
    
    static String readFile(URL fileURL) throws IOException,FileNotFoundException {
        return new String(Files.readAllBytes(Paths.get(fileURL.getPath())), StandardCharsets.UTF_8);
    }
    
    public void parse() throws ScriptException, IOException, NoSuchMethodException {
    	FileOutputStream fout = new FileOutputStream(arquivoJSON);
    	PrintStream bckprint = System.out;
    	PrintStream printst = new PrintStream(fout);
    	System.setOut(printst); 

    	
    	ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");

        ScriptContext context = engine.getContext();
        
        Bundle bundle = Platform.getBundle("utilfuncplugin");
        URL eclipseURL = FileLocator.find(bundle, new Path("/rinoceronte/esprima.js"), null);
        URL fileURL = FileLocator.toFileURL(eclipseURL);
        engine.eval(readFile(fileURL), context);

        //engine.eval(readFile("/rinoceronte/esprima.js"), context);

        
       // context.setAttribute("__dirname", "/home/foo", ENGINE_SCOPE);
       // context.setAttribute("__filename", "client.js", ENGINE_SCOPE);
        
        
        Invocable inv = (Invocable) engine;
        Object esprima = engine.get("esprima");
        engine.eval("args = {loc: true, range: true, tokens: true, comment: true}");
        Object tree = inv.invokeMethod(esprima, "parse", readFile(arquivoJS.getPath()), engine.eval("args"));
        
        Object JSON = engine.get("JSON");
        
        /*Invocable inv = (Invocable) engine;
        Object esprima = engine.get("esprima");
        
        Object tree = inv.invokeMethod(esprima, "parse", readFile(arquivoJS.getPath()));
        
        Object JSON = engine.get("JSON");
		*/
        String json = (String) inv.invokeMethod(JSON, "stringify", tree, null, 2);
        System.out.println(json);
        System.setOut(bckprint);
    }
}
