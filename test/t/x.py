class Name(object):
    def __init__(self, name):
        self.__name = name
        
    def compose(self):
        return self.__name + "@" + self.__rest.compose()
    
    def __at__(self, rest):
        self.__rest = rest
        return self
        
class Domain(object):
    def __init__(self, address, domain, domain_name="com"):
        self.__address = address
        if domain is not None:
            self.__domain = domain
            self.__chain = None
            setattr(self, domain_name, domain)
        else:
            self.__chain = None
            self.__domain = None
            self.__body = ""
            self.__header = ""
        
    def compose(self):
        if self.__chain is not None:
            return self.__chain + "." + self.__address + " Header -> " + self.__header + ", body: " + self.__body
        else:
            return self.__address + " Header -> " + self.__header + ", body: " + self.__body
        
    def __lshift__(self, data):
        self.__header, self.__body = data
        return self
    
    def __getattribute__(self, attr):
        if attr == "self" or attr.startswith("__") or attr == "as_chain" or attr == "compose": return object.__getattribute__(self, attr)
        return object.__getattribute__(self, attr).as_chain(self.__address)
    
    def as_chain(self, component):
        self.__chain = component
        return self

class Mailto(object):
    def __block__(self, x):
       message = x()
       self.do_mail(message)
       
    def do_mail(self, mail):
        print mail, mail.compose()
    
mailto = Mailto()
admin = Name("admin")
com = Domain("com", None)
example = Domain("example", com, "com") 
       
def test():   
    mailto: admin@example.com << ("Hello!", "Friendly message from admin!")  