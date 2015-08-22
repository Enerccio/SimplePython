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

__all__ = ["error", "timeout", "socket", "gethostname", "SOCK_DGRAM", "SOCK_STREAM"]

class error(IOError):
    pass

class timeout(Exception):
    pass

SOCK_STREAM = 1
SOCK_DGRAM  = 0

__sockethelper = javainstance("__sockethelper__", error)

def gethostname():
    return __sockethelper.gethostname()

def socket(type=SOCK_STREAM, timeout=0):
    if type == SOCK_STREAM:
        return _tcp_socket(timeout)
    else:
        return _udp_socket(timeout)
    
def create_connection(address, timeout, source_address):
    pass

class _udp_socket(object):
    def __init__(self, to=0):
        self.__closed = False
        self.__sock = javainstance("__udpsocket__", error, timeout)
        self.__sock.timeout(to)
        
    def __enter__(self):
        self.check_closed()
        return self
    
    def __exit__(self, type, instance):
        self.check_closed()
        self.close()
        
    def check_closed(self):
        if self.__closed:
            raise error("closed")
        
    def sendto(self, message, addr):
        self.check_closed()
        self.__sock.sendto(message, addr)
        
    def bind(self, addr):
        self.check_closed()
        self.__sock.bindTo(addr[0], addr[1])
        
    def recvfrom(self, bufsize):
        self.check_closed()
        data, address = self.__sock.recv(bufsize)
        return data, address

class _tcp_socket(object):
    def __init__(self, timeout=0):
        self.__sock = None
        self.__bind_addr = None
        self.__closed = False
        self.__server = False
        self.__timeout = timeout
        
    def __enter__(self):
        self.check_closed()
        return self
    
    def __exit__(self, type, instance):
        self.check_closed()
        self.close()
        
    def check_closed(self):
        if self.__closed:
            raise error("closed")
        
    def accept(self):
        self.check_closed()
        if not self.__sock:
            raise error("unbound socket")
        if not self.__server:
            raise error("not a server socket")
        ns, address = self.__sock.accept()
        sock = socket()
        sock.__type = self.__type
        sock.__sock = ns
        return sock, address
        
    def close(self):
        self.check_closed()
        if self.__sock is not None:
            self.__sock.close()
    
    def bind(self, address):
        self.check_closed()
        self.__bind_addr = address
        
    def listen(self, backlog=5):
        self.check_closed()
        if self.__sock is not None:
            raise error("socket already created!")
        self.__sock = javainstance("__serversocket__", error, timeout, self.__bind_addr[0], self.__bind_addr[1], backlog)
        self.__sock.timeout(self.__timeout)
        self.__server = True
    
    def connect(self, address):
        self.check_closed()
        if self.__sock is not None:
            raise error("socket already created!")
        self.__bind_addr = address
        self.__sock = javainstance("__clientsocket__", error, timeout, self.__bind_addr[0], self.__bind_addr[1])
        self.__sock.connect()
        
    def connect_ex(self, address):
        self.check_closed()
        if self.__sock is not None:
            raise error("socket already created!")
        self.__bind_addr = address
        self.__sock = javainstance("__clientsocket__", error, timeout, self.__bind_addr[0], self.__bind_addr[1])
        self.__sock.connect_ex()