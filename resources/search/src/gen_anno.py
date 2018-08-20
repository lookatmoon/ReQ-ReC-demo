import sys
from sets import Set

if len(sys.argv) != 6:
    exit ('Param: tsv model map top_prf output')

tsv_f = open(sys.argv[1])
mdl_f = open(sys.argv[2])
map_f = open(sys.argv[3])
top_k = int(sys.argv[4])
out_f = open(sys.argv[5], 'w')

# read in the map
oid_cid = {}
while True:
    line = map_f.readline()
    if not line:
	break
    ss = line.strip().split(':')
    oid_cid[int(ss[0])] = int(ss[1])

# read in the centroids from the model
centroid = Set([])
while True:
    line = mdl_f.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    centroid.add(int(ss[1]))

# include the top retrieved tweets: 
for i in sorted(oid_cid.keys())[0:top_k]:
    cid = oid_cid[i]
    centroid.add(cid)

# read in the tsv and output the format for annotation
i = 1
while True:
    line = tsv_f.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    if i in oid_cid: # filter out those tweets discarded by the clustering
	cid = oid_cid[i]
	if cid in centroid:
	    label = -2
	else:
	    label = 0
	# out_f.write('{}\t{}\t{}\n'.format(label, ss[0], ss[1]))
	out_f.write('{}\t{}\t{}\n'.format(label, ss[1], ss[2]))
    i += 1
tsv_f.close()
mdl_f.close()
map_f.close()
