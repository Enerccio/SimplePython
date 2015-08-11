def test():
	def inner(anca):
		print anca
		print locals()
	inner(locals())