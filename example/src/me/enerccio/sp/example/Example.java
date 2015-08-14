package me.enerccio.sp.example;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.pointer.WrapPublicFactory;
import me.enerccio.sp.utils.Coerce;

public class Example extends JFrame {
	private static final long serialVersionUID = 3581091849784539787L;

	public static void main(String[] args){
		SimplePython.initialize();
		SimplePython.setAllowAutowraps(true);
		SimplePython.addAlias(ButtonQuitEvent.class, "btnq_event");
		SimplePython.addAlias(ButtonTestEvent.class, "btnt_event");
		SimplePython.addFactory("", WrapPublicFactory.class);
		SimplePython.addResolver(PythonPathResolver.make(new File("").getAbsolutePath()));
		SimplePython.getModule("example1");
	}
	
	public static class ButtonQuitEvent implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			PythonObject exitFnc = SimplePython.getModule("example1").getField("exit");
			PythonObject eventOwner = Coerce.toPython(e.getSource(), e.getSource().getClass());
			SimplePython.executeFunction("example1", "add_event", exitFnc, eventOwner, SimplePython.asTuple(new ArrayList<Object>()));
		}
		
	}
	
	public static class ButtonTestEvent implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			PythonObject exitFnc = SimplePython.getModule("example1").getField("show_alert");
			PythonObject eventOwner = Coerce.toPython(e.getSource(), e.getSource().getClass());
			List<String> args = new ArrayList<String>();
			args.add("Hello, world!");
			SimplePython.executeFunction("example1", "add_event", exitFnc, eventOwner, SimplePython.asTuple(args));
		}
		
	}
}
