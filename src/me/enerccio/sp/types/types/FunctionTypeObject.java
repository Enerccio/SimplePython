package me.enerccio.sp.types.types;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.parser.pythonLexer;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;
import me.enerccio.sp.utils.Utils.ThrowingErrorListener;

public class FunctionTypeObject extends TypeObject {
	private static final long serialVersionUID = 4621194768946693335L;
	public static final String FUNCTION_CALL = "function";

	@Override
	public String getTypeIdentificator() {
		return "function";
	}

	// function(string, locals, tuple_of_maps, list_of_anames, vararg_name, dict)
	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() != 6)
			throw Utils.throwException("TypeError", " function(): incorrect number of parameters, requires 6, got " + args.len());
		
		String src = null;
		MapObject dict = null;
		List<MapObject> maps = new ArrayList<MapObject>();
		List<String> aas = new ArrayList<String>();
		String vararg = null;
		MapObject defaults = null;
		
		try {
			PythonObject arg = args.getObjects()[0];
			src = ((StringObject)arg).value;
			
			dict = (MapObject)args.getObjects()[1];
			
			TupleObject to = (TupleObject)args.getObjects()[2];
			for (PythonObject o : to.getObjects())
				maps.add(((MapObject)o));
			
			ListObject o = (ListObject)args.getObjects()[3];
			for (PythonObject oo : o.objects)
				aas.add(((StringObject)oo).value);
			
			arg = args.getObjects()[4];
			if (arg != NoneObject.NONE)
				vararg = ((StringObject)arg).value;
			
			arg = args.getObjects()[5];
			if (arg != NoneObject.NONE)
				defaults = (MapObject)arg;
			else
				defaults = new MapObject();
			
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", " function(): wrong types of arguments");
		}
		
		PythonCompiler c = new PythonCompiler();
		
		ANTLRInputStream is = new ANTLRInputStream(src);
		pythonLexer lexer = new pythonLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new ThrowingErrorListener("<generated>"));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		pythonParser parser = new pythonParser(stream);
		
		parser.removeErrorListeners();
		parser.addErrorListener(new ThrowingErrorListener("<generated>"));
		
		return c.doCompile(parser.string_input(), maps, aas, vararg, defaults, dict);
	}

}
