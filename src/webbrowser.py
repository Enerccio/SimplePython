"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
webbrowser module
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.
 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public
License along with this library.
"""

__all__ = ["OPEN_NEW", "OPEN_NEW_TAB", "OPEN_DEFAULT", "Error", "open", "open_new", "open_new_tab", "get", "register"]

OPEN_DEFAULT = 0
OPEN_NEW = 1
OPEN_NEW_TAB = 2

class Error(Exception):
    """
    Thrown if webbrowser functions fail
    """
    pass

class controller(object):
    """
    controller object is base class for a browser type
    """
    def __init__(self, name=None):
        self.browser = javainstance("__webbrowser__", name, Error) 
        
    def open(self, url, new=OPEN_DEFAULT, autoraise=True):
        """
        Opens browser with url, autoraise is unfortunatelly ignored and 
        OPEN_NEW_TAB is ignored as well 
        """
        self.browser.open(url, new, autoraise)
    
    def open_new(self, url):
        """
        Does same as open with new=OPEN_NEW
        """
        self.browser.open(url, OPEN_NEW)
        
    def open_new_tab(self, url):
        """
        Does same as open with new=OPEN_DEFAULT due to limitations of underline java library
        """
        self.browser.open(url, OPEN_NEW_TAB)
         
class Mozilla(controller):
    pass

class Galeon(controller):
    pass

class BackgroundBrowser(controller):
    pass

class Konqueror(controller):
    def __init__(self):
        controller.__init__(self, "kfm")
        
class Opera(controller):
    def __init__(self):
        controller.__init__(self, "opera")
        
class Grail(controller):
    def __init__(self):
        controller.__init__(self, "grail")
        
class GenericBrowser(controller):
    pass

class Elinks(controller):
    pass

class WindowsDefault(controller):
    pass

class MacOSX(controller):
    pass

__mappings = {
    'mozilla'           : Mozilla('mozilla'),
    'firefox'           : Mozilla('firefox'),
    'netscape'          : Mozilla('netscape'),
    'galeon'            : Galeon('galeon'),
    'epiphany'          : Galeon('epiphany'),
    'skipstone'         : BackgroundBrowser('skipstone'),     
    'kfmclient'         : Konqueror(),
    'konqueror'         : Konqueror(),     
    'kfm'               : Konqueror(),     
    'mosaic'            : BackgroundBrowser('mosaic'),      
    'opera'             : Opera(),      
    'grail'             : Grail(),      
    'links'             : GenericBrowser('links'),      
    'elinks'            : Elinks('elinks'),      
    'lynx'              : GenericBrowser('lynx'),      
    'w3m'               : GenericBrowser('w3m'),      
    'windows-default'   : WindowsDefault(),
    'macosx'            : MacOSX('default'),    
    'safari'            : MacOSX('safari'),     
}
__default = controller()

def open(url, new=OPEN_DEFAULT, autoraise=True):
    """
    Opens browser with url, autoraise is unfortunatelly ignored and 
    OPEN_NEW_TAB is ignored as well 
    """
    __default.open(url, new, autoraise)
    
def open_new(url):
    """
    Does same as open with new=OPEN_NEW
    """
    open(url, OPEN_NEW)
    
def open_new_tab(url):
    """
    Does same as open with new=OPEN_DEFAULT due to limitations of underline java library
    """
    open(url, OPEN_NEW_TAB)
    
def get(name=None):
    """
    Returns controller for the name. If name is None, returns default controller
    """
    if name is None:
        return __default
    return __mappings[name]

def register(name, constructor, instance=None):
    """
    Registers new controller for the name. If instance is not None,
    instance is registered, otherwise constructor is called with 0 arguments
    to create the instance to register
    """
    if instance is not None:
        __mappings[name] = instance
    else:
        __mappings[name] = constructor()
