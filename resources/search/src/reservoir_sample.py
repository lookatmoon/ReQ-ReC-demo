import sys
import random

if len(sys.argv) != 2:
	exit ('Params: size')

size = int(sys.argv[1])
res = []
i = 1
while True:
	line = sys.stdin.readline()
	if not line:
		break
	if i <= size:
		res.append( (i, line) )
	else:
		# the line enters with prob. size/i
		if random.random() <= float(size)/float(i):
			res[random.randint(0, size - 1)] = (i, line)
	i += 1
	
	if i % 100000 == 0:
	    sys.stderr.write( str(i) + '\n')

res.sort(key = lambda tup: tup[0])
for t in res:
	sys.stdout.write(t[1])
