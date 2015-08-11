def test():
	try:
		print "%*.1d" % (3, 3.14512)
	except Error, e:
		raise e