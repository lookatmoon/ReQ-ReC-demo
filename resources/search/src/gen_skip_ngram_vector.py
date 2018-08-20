import sys
from itertools import combinations

if len(sys.argv) < 5:
    exit('Params: binary(TRUE|FALSE) window_size ngram_sizes(e.g., 2,3,4) ngram_list1 ngram_list2 ...(in order)')
    
ngram_sizes = sys.argv[3].split(',')
if len(ngram_sizes) + 4 != len(sys.argv):
    exit('Check if all ngram_list files are provided in order')

if sys.argv[1] == 'TRUE':
    binary = True
else:
    binary = False
    
win_sz = int(sys.argv[2])
ngrams = {}
orders = []
id = 1
for i in range(len(ngram_sizes)):
    f = open(sys.argv[4 + i])
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
	    if l < win_sz:
		win = l
	    else:
		win = win_sz
	    # iterate through the windows in 'good'
	    # k == 0
	    for sk in combinations(ss[0:win], order_n):
		sk_ngr = ' '.join(sk)
		try:
			id = ngrams[sk_ngr]
			if id in fv:
				if not binary:
					fv[id] += 1
			else:
				fv[id] = 1
		except:
			continue
	    # k > 0
	    for k in range(1, l - win + 1):
		last_uni = ' ' + ss[k+win-1]
		for sk in combinations(ss[k : k+win-1], order_n-1):
		    sk_ngr = ' '.join(sk) + last_uni
		    try:
			    id = ngrams[sk_ngr]
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
