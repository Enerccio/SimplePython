def y(**kwargs):
	print kwargs

def test():
	x = 15
	exec "print y(anca=5, mara=x)"
	y = compile("print y", "anca")
	print y
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