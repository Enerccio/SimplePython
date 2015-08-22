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
    pass

class controller(object):
    def __init__(self, name=None):
        self.browser = javainstance("__webbrowser__", name, Error) 
        
    def open(self, url, new=OPEN_DEFAULT, autoraise=True):
         self.browser.open(url, new, autoraise)
    
    def open_new(self, url):
         self.browser.open(url, OPEN_NEW)
        
    def open_new_tab(self, url):
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
    __default.open(url, new, autoraise)
    
def open_new(url):
    open(url, OPEN_NEW)
    
def open_new_tab(url):
    open(url, OPEN_NEW_TAB)
    
def get(name=None):
    if name is None:
        return __default
    return __mappings[name]

def register(name, constructor, instance=None):
    if instance is not None:
        __mappings[name] = instance
    else:
        __mappings[name] = constructor()
