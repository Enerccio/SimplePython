def y(**kwargs):
	print kwargs
	
print __name__
print __thismodule__

def test():
	print ast.BYTECODE_NAMES
	print ast.BYTECODE_NUMBERS
	
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