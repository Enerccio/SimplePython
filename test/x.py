def test():
	W().test()

class W(object):
	def __init__(self):
		super(W, self).__init__()
		
	def test(self):
		y = compile("print 10", "anca");
		print y
		print dir(y)
		print y.co_code
		print y.co_consts
		print y.co_debug
		print bytecode(0)
		print dir(bytecode(0))
		bytecode(0).value = 5
		
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