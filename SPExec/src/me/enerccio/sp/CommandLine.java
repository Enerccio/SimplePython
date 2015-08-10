/*  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.enerccio.sp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;
import me.enerccio.sp.utils.StaticTools.ParserGenerator;

public class CommandLine {

	private BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
	
	public void run() throws Exception {
		DictObject locals = generateLocals();
		System.out.println(String.format("SimplePython %s %s:%s%s", 
				SimplePython.conformingVersion(), SimplePython.mainVersion(), SimplePython.minorVersion(), SimplePython.revisionNumber()));
		System.out.println("Type \"help\", \"copyright\", \"credits\" or \"license\" for more information");
		while (true){
			try {
				execute(locals);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void execute(DictObject locals) throws Exception {
		System.err.flush();
		System.out.flush();
		String nextExecutionCode = getNext();
		PythonObject last = execute(nextExecutionCode, locals);
		if (last == null | last == NoneObject.NONE)
			last = PythonInterpreter.interpreter.get().lastPushedValue;
		if (last != null && last != NoneObject.NONE){
			TupleObject t = new TupleObject(last);
			t.newObject();
			Utils.run("print_function", t);
		}
	}

	private PythonObject execute(String nextExecutionCode, DictObject locals) throws Exception {
		ModuleProvider mp = new ModuleProvider("<stdin>", "System.in", nextExecutionCode.getBytes(), "", false);
		pythonParser p = ParserGenerator.parse(mp);
		CompiledBlockObject block = new PythonCompiler().doCompileReadline(p.file_input(), "<stdin>");
		
		PythonInterpreter.interpreter.get().setClosure(Arrays.asList(new DictObject[]{locals, PythonRuntime.runtime.getGlobals()}));
		PythonInterpreter.interpreter.get().executeBytecode(block);
		return PythonInterpreter.interpreter.get().executeAll(0, true);
	}

	private String getNext() throws Exception {
		System.out.print(">>> ");
		return readNextFullStatement();
	}

	private String readNextFullStatement() throws Exception {
		// TODO more
		return r.readLine();
	}

	private DictObject generateLocals() throws Exception {
		DictObject dict = new DictObject();
		
		// TODO help, copyring, credits, license
		dict.put("exit", JavaMethodObject.noArgMethod(this, "exit"));
		
		return dict;
	}

	public static void exit(){
		System.exit(0);
	}
}
