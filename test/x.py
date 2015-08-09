def test():
	W().test()

import dis
import ast

class W(object):
	def __init__(self):
		super(W, self).__init__()
		
	def test(self):
		y = compile("print 10\na = 5\nprint a\ndef aaa(): \n\tprint aaa\naaa()\nraise ValueError, 'test'", "anca");
		dis.dis(y)
		exec y 
		
		"""
		x = 1
		print x.__format__("b")
		print x.__format__("c")
		print x.__format__("d")
		print x.__format__("o")
		print x.__format__("x")
		print x.__format__("X")
		print x.__format__("n")
		print x.__format__("")
		"""	