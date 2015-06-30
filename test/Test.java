import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;


public class Test {
	
	public static void main(String[] args){
		PythonInterpret i = new PythonInterpret();
		PythonObject v = i.executeCall("str", new IntObject(10));
	}

}
