import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.sequences.StringObject;


public class Test {
	
	public static void main(String[] args){
		PythonInterpret i = new PythonInterpret();
		PythonObject v = i.executeCall("getattr", new IntObject(10), new StringObject("__int__"));
		v.toString();
	}

}
