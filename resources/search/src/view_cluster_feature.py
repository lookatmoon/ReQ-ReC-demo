import sys
import operator

if len(sys.argv) != 5:
	exit ('Params: feature cluster_model top_k output')

fea_f = open(sys.argv[1])
mdl_f = open(sys.argv[2])
top_k = int(sys.argv[3])
out_f = open(sys.argv[4], 'w')

# load the feature
i = 1
fea_str = {}
while True:
	line = fea_f.readline()
	if not line:
		break
	fea_str[i] = line.strip().split('\t')[0]
	i += 1

# load the models
while True:
	line = mdl_f.readline()
	if not line:
		break
	ss = line.strip().split('\t')
	fea_val = {}

	# construct dict for each cluster
	for fv in ss[4].split(' '):
		part = fv.split(':')
		fea_val[int(part[0])] = float(part[1])
	
	# print out the top
	sorted_fea_val = sorted(fea_val.iteritems(), key=operator.itemgetter(1), reverse=True)
	out_f.write('{}\t'.format(ss[0]))
	for i in range(top_k):
		out_f.write('{}| '.format( fea_str[sorted_fea_val[i][0]] ))
	out_f.write('\n')

fea_f.close()
mdl_f.close()
out_f.close()
