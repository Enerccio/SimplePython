import socket

def test():
   UDP_IP = "127.0.0.1"
   UDP_PORT = 50050
   MESSAGE = "Hello, World!"
   
   print "UDP target IP:", UDP_IP
   print "UDP target port:", UDP_PORT
   
   sock = socket.socket(socket.SOCK_DGRAM) # UDP
   sock.bind((UDP_IP, UDP_PORT))
    
   while True:
       data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
       print "received message:", data