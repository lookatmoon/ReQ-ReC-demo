import sys

if len(sys.argv) < 4:
	exit('Params: binary(TRUE|FALSE) ngram_sizes(e.g., 2,3,4) ngram_list1 ngram_list2 ...(in order)')
	
ngram_sizes = sys.argv[2].split(',')
if len(ngram_sizes) + 3 != len(sys.argv):
	exit('Check if all ngram_list files are provided in order')

if sys.argv[1] == 'TRUE':
	binary = True
else:
	binary = False
	
ngrams = {}
orders = []
id = 1
for i in range(len(ngram_sizes)):
	f = open(sys.argv[3 + i])
	while True:
		line = f.readline()
		if not line:
			break
		ngrams[line.split('\t')[0]] = id
		id += 1
	f.close()
	orders.append(int(ngram_sizes[i]))

while True:
	line = sys.stdin.readline()
	if not line:
		break
	ss = line.strip().split(' ')
	l = len(ss)
	fv = {}
	for order_n in orders:
		for k in range(l - order_n + 1): # [k:k+n+1]
			ngr = ' '.join(ss[k : k+order_n])
			try:
				id = ngrams[ngr]
				if id in fv:
					if not binary:
						fv[id] += 1
				else:
					fv[id]  = 1
			except:
				continue
	for id in sorted(fv.iterkeys()):
		sys.stdout.write(str(id) + ':' + str(fv[id]) + ' ')
	sys.stdout.write('\n')
