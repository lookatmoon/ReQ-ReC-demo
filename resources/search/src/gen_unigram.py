import sys
from sets import Set

uni_dict = {}
uni_docf = {}
# read lines from stdin
while True:
	line = sys.stdin.readline()
	if not line:
		break
	ss = line.strip().split(' ')
	uniq = Set([])
	for w in ss:
		if w not in uni_dict:
			uni_dict[w] = 1
		else:
			uni_dict[w] += 1
		# doc freq
		if w not in uniq:
		    uniq.add(w)
		    if w not in uni_docf:
			uni_docf[w] = 1
		    else:
			uni_docf[w] += 1

			
try: uni_dict.pop('')
except: i = 0

for w in uni_dict:
	sys.stdout.write(w + '\t' + str(uni_dict[w]) + '\t' + str(uni_docf[w]) + '\n')
