import eventlib
import os

def exit(object):
    print object
    os._exit(0)
    
def show_alert(object, *args):
    print object
    e.frame.showDialog(*args)
    

class example(object):
    def __init__(self):
        self.frame = javainstance("me.enerccio.sp.example.Example");
        
        self.frame.setSize(250, 200)
        self.frame.setTitle("Application")
        self.frame.setDefaultCloseOperation(3)
        self.frame.setLocationRelativeTo(None)
        self.frame.setVisible(True)
        
        cbtn = javainstance("javax.swing.JButton", "Click me!")
        cbtn.addActionListener(javainstance("btnt_event"))
        
        qbtn = javainstance("javax.swing.JButton", "quit")
        qbtn.addActionListener(javainstance("btnq_event"))
        
        self.create_layout(cbtn, qbtn)
        
        self.frame.setDefaultCloseOperation(self.frame.EXIT_ON_CLOSE)
        
    def create_layout(self, *components):
        panel = javainstance("javax.swing.JPanel")
        
        for c in components:
            panel.add(c)
            
        self.frame.add(panel)
        self.frame.pack()

eventlib.standard_events()
e = example()

def event_handler(event, callable, po, args):
    print "event handler - ", event
    callable(po, *args)

def add_event(callable, po, args):
    eventlib.event_queue << eventlib.Event(event_handler, args=(callable, po, args))