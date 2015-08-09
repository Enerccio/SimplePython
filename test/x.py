def y(**kwargs):
	print kwargs
	
# import ast

def yielder():
	for x in xrange(3):
		yield -x

a, b, c = yielder()
print a, b, c

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