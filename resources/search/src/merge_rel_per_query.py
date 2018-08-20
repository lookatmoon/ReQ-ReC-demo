import sys
import re
from sets import Set

if len(sys.argv) != 6:
    exit ('Params: rel partition(w/ duplicate keys) qid run_id output')

rel_f = open(sys.argv[1])
par_f = open(sys.argv[2])
qid = sys.argv[3]
rid = sys.argv[4]
out_f = open(sys.argv[5], 'w')

# partition: rep_tid tweet {tid}
partition = {}
while True:
    line = par_f.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    if ss[0] not in partition:
	partition[ss[0]] = ss[2].split(' ')
par_f.close()

# rel: label conf tid tweet
i = 1
seen = Set([])
while True:
    line = rel_f.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    # if ss[0] == '1':
    
    # remove retweets, assuming 'RT' appearing in the front 8 characters
    m = re.search(r'^.{0,8}RT', ss[3])
    if m != None:
	continue
    # the representative relevant tweet
    if ss[2] not in seen:
	out_f.write('{} {} {} {} {} {}\t{}\n'.format(qid, 'Q0', ss[2], i, ss[1], rid, ss[3]))
	i += 1
	seen.add(ss[2])
    if ss[2] in partition:
	if len(partition[ss[2]]) > 1:
	    # the other tweets in the partition
	    for tid in partition[ss[2]][1:]:
		if ss[2] not in seen:
		    out_f.write('{} {} {} {} {} {}\n'.format(qid, 'Q0', tid,  i, ss[1], rid))
		    i += 1
		    seen.add(ss[2])

rel_f.close()
out_f.close()
