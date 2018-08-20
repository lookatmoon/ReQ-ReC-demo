import sys
import math
from sets import Set

if len(sys.argv) != 3:
	exit('Params: unigram_list order_n')

uni_f   = open(sys.argv[1])
order_n = int(sys.argv[2])

# read in unigrams: word - id list
uni_dict = {}
i = 0
while True:
	line = uni_f.readline()
	if not line:
		break
	ss = line.strip().split('\t')
	uni_dict[ss[0]] = i
	i += 1
uni_f.close()

def gen_ngr_id(grams):
	return ' '.join(grams)

# generate n-grams
ngr_dict = {}
ngr_docf = {}
while True:
	line = sys.stdin.readline()
	if not line:
		break
	# print line
	ss = line.strip().split(' ')
	good = []
	for s in ss:
		if s in uni_dict:
			good.append(s)
	l = len(good)
	# for i in range(l):
	uniq = Set([])
	for k in range(l - order_n + 1): # [k:k+n+1]
		ngr_id = gen_ngr_id(good[k : k+order_n])
		# print ngr_id
		if ngr_id not in ngr_dict:
			ngr_dict[ngr_id] = 1
		else:
			ngr_dict[ngr_id] += 1
		if ngr_id not in uniq:
		    uniq.add(ngr_id)
		    if ngr_id not in ngr_docf:
			ngr_docf[ngr_id] = 1
		    else:
			ngr_docf[ngr_id] += 1
# uni_list = uni_dict.keys()
# print ngr_dict
for id in ngr_dict:
	sys.stdout.write(id + '\t' + str(ngr_dict[id]) + '\t' + str(ngr_docf[id]) + '\n')
