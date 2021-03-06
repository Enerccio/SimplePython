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

# convert UPPER CASE letters to lower case
def lower(s):
    """lower(s) -> string

    Return a copy of the string s converted to lowercase.
    """
    return s.lower()

# Convert lower case letters to UPPER CASE
def upper(s):
    """upper(s) -> string

    Return a copy of the string s converted to uppercase.
    """
    return s.upper()

# Swap lower case letters and UPPER CASE
def swapcase(s):
    """swapcase(s) -> string

    Return a copy of the string s with upper case characters
    converted to lowercase and vice versa.
    """
    return s.swapcase()

# Strip leading and trailing tabs and spaces
def strip(s, chars=None):

    """strip(s [,chars]) -> string

    Return a copy of the string s with leading and trailing
    whitespace removed.

    If chars is given and not None, remove characters in chars instead.
    
    If chars is unicode, S will be converted to unicode before stripping.
    """
    return s.strip(chars)

# Strip leading tabs and spaces
def lstrip(s, chars=None):
    """lstrip(s [,chars]) -> string

    Return a copy of the string s with leading whitespace removed.
    If chars is given and not None, remove characters in chars instead.
    """
    return s.lstrip(chars)

# Strip trailing tabs and spaces
def rstrip(s, chars=None):
    """rstrip(s [,chars]) -> string

    Return a copy of the string s with trailing whitespace removed.
    If chars is given and not None, remove characters in chars instead.
    """
    return s.rstrip(chars)

# Split a string into a list of space/tab-separated words
def split(s, sep=None, maxsplit=-1):
    """split(s [,sep [,maxsplit]]) -> list of strings
    
    Return a list of the words in the string s, using sep as the
    delimiter string.  If maxsplit is given, splits at no more than
    maxsplit places (resulting in at most maxsplit+1 words).  If sep
    is not specified or is None, any whitespace string is a separator.
    (split and splitfields are synonymous)
    """
    return s.split(sep, maxsplit)
splitfields = split

# Split a string into a list of space/tab-separated words
def rsplit(s, sep=None, maxsplit=-1):
    """rsplit(s [,sep [,maxsplit]]) -> list of strings
    
    Return a list of the words in the string s, using sep as the
    delimiter string, starting at the end of the string and working
    to the front.  If maxsplit is given, at most maxsplit splits are
    done. If sep is not specified or is None, any whitespace string
    is a separator.
    """
    return s.rsplit(sep, maxsplit)

# Join fields with optional separator
def join(words, sep = ' '):
    """join(list [,sep]) -> string
    
    Return a string composed of the words in list, with
    intervening occurrences of sep.  The default separator is a
    single space.
    (joinfields and join are synonymous)
    """
    return sep.join(words)
joinfields = join

# Find substring, raise exception if not found
def index(s, *args):
    """index(s, sub [,start [,end]]) -> int
    
    Like find but raises ValueError when the substring is not found.
    """
    return s.index(*args)

# Find last substring, raise exception if not found
def rindex(s, *args):
    """rindex(s, sub [,start [,end]]) -> int
    
    Like rfind but raises ValueError when the substring is not found.
    """
    return s.rindex(*args)

# Count non-overlapping occurrences of substring
def count(s, *args):
    """count(s, sub[, start[,end]]) -> int
    
    Return the number of occurrences of substring sub in string
    s[start:end].  Optional arguments start and end are
    interpreted as in slice notation.
    """
    return s.count(*args)

# Find substring, return -1 if not found
def find(s, *args):
    """find(s, sub [,start [,end]]) -> in
    
    Return the lowest index in s where substring sub is found,
    such that sub is contained within s[start,end].  Optional
    arguments start and end are interpreted as in slice notation.
    Return -1 on failure.
    """
    return s.find(*args)

# Find last substring, return -1 if not found
def rfind(s, *args):
    """rfind(s, sub [,start [,end]]) -> int
    
    Return the highest index in s where substring sub is found,
    such that sub is contained within s[start,end].  Optional
    arguments start and end are interpreted as in slice notation.
    Return -1 on failure.
    """
    return s.rfind(*args)

# for a bit of speed
_float = float
_int = int
_long = int

# Convert string to float
def atof(s):
    """atof(s) -> float
    
    Return the floating point number represented by the string s.
    """
    return _float(s)

# Convert string to integer
def atoi(s , base=10):
    """atoi(s [,base]) -> int
    
    Return the integer represented by the string s in the given
    base, which defaults to 10.  The string s must consist of one
    or more digits, possibly preceded by a sign.  If base is 0, it
    is chosen from the leading characters of s, 0 for octal, 0x or
    0X for hexadecimal.  If base is 16, a preceding 0x or 0X is
    accepted.
    """
    return _int(s, base)

# Convert string to long integer
def atol(s, base=10):
    """atol(s [,base]) -> long
    
    Return the long integer represented by the string s in the
    given base, which defaults to 10.  The string s must consist
    of one or more digits, possibly preceded by a sign.  If base
    is 0, it is chosen from the leading characters of s, 0 for
    octal, 0x or 0X for hexadecimal.  If base is 16, a preceding
    0x or 0X is accepted.  A trailing L or l is not accepted,
    unless base is 0.
    """
    return _long(s, base)

# Left-justify a string
def ljust(s, width, *args):
    """ljust(s, width[, fillchar]) -> string
    
    Return a left-justified version of s, in a field of the
    specified width, padded with spaces as needed.  The string is
    never truncated.  If specified the fillchar is used instead of spaces.
    """
    return s.ljust(width, *args)

# Right-justify a string
def rjust(s, width, *args):
    """rjust(s, width[, fillchar]) -> string
    
    Return a right-justified version of s, in a field of the
    specified width, padded with spaces as needed.  The string is
    never truncated.  If specified the fillchar is used instead of spaces.
    """
    return s.rjust(width, *args)

# Center a string
def center(s, width, *args):
    """center(s, width[, fillchar]) -> string
    
    Return a center version of s, in a field of the specified
    width. padded with spaces as needed.  The string is never
    truncated.  If specified the fillchar is used instead of spaces.
    """
    return s.center(width, *args)

# Zero-fill a number, e.g., (12, 3) --> '012' and (-3, 3) --> '-03'
# Decadent feature: the argument may be a string or a number
# (Use of this is deprecated; it should be a string as with ljust c.s.)
def zfill(x, width):
    """zfill(x, width) -> string
    
    Pad a numeric string x with zeros on the left, to fill a field
    of the specified width.  The string x is never truncated.
    """
    return x.zfill(width)

# Expand tabs in a string.
# Doesn't take non-printing chars into account, but does understand \n.
def expandtabs(s, tabsize=8):
    """expandtabs(s [,tabsize]) -> string
    
    Return a copy of the string s with all tab characters replaced
    by the appropriate number of spaces, depending on the current
    column, and the tabsize (default 8).
    """
    return s.expandtabs(tabsize)

# Character translation through look-up table.
def translate(s, table, deletions=""):
    """translate(s,table [,deletions]) -> string
    
    Return a copy of the string s, where all characters occurring
    in the optional argument deletions are removed, and the
    remaining characters have been mapped through the given
    translation table, which must be a string of length 256.  The
    deletions argument is not allowed for Unicode strings.
    """
    if deletions or table is None:
        return s.translate(table, deletions)
    else:
        # Add s[:0] so that if s is Unicode and table is an 8-bit string,
        # table is converted to Unicode.  This means that table *cannot*
        # be a dictionary -- for that feature, use u.translate() directly.
        return s.translate(table + s[:0])

# Capitalize a string, e.g. "aBc  dEf" -> "Abc  def".
def capitalize(s):
    """capitalize(s) -> string
    
    Return a copy of the string s with only its first character
    capitalized.
    """
    return s.capitalize()

# Substring replacement (global)
def replace(s, old, new, maxreplace=-1):
    """replace (str, old, new[, maxreplace]) -> string
    
    Return a copy of string str with all occurrences of substring
    old replaced by new. If the optional argument maxreplace is
    given, only the first maxreplace occurrences are replaced.

    """
    return s.replace(old, new, maxreplace)

class Formatter(object):
    def format(self, format_string, *args, **kwargs):
        self.vformat(format_string, args, kwargs)
        
    def vformat(self, format_string, args, kwargs):
        return (javainstance("__formatter__", self.get_value, self.check_unused_args, self.format_field)
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
