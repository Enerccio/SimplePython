"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
 string module
 
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

Copied from python's string.py
"""

whitespace = ' \t\n\r\v\f'
lowercase = 'abcdefghijklmnopqrstuvwxyz'
uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
letters = lowercase + uppercase
ascii_lowercase = lowercase
ascii_uppercase = uppercase
ascii_letters = ascii_lowercase + ascii_uppercase
digits = '0123456789'
hexdigits = digits + 'abcdef' + 'ABCDEF'
octdigits = '01234567'
punctuation = """!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~"""
printable = digits + letters + punctuation + whitespace

## COPY OVER TO copy.copy

class Formatter(object):
    def format(self, format_string, *args, **kwargs):
        self.vformat(format_string, args, kwargs)
        
    def vformat(self, format_string, args, kwargs):
        return (javainstance("formatter", self.get_value, self.check_unused_args, self.format_field)
                    .doFormat(format_string, args, kwargs))
    
    def get_value(self, key, args, kwargs):
        if type(key) == str:
            return kwargs[key]
        else:
            return args[key]
        
    def check_unused_args(self, used_ags, args, kwargs):
        pass
    
    def format_field(self, value, format_spec):
        return format(value, format_spec)
