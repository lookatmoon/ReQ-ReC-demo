import sys
from sets import Set

partition = {}
keys = []

while True:
    line = sys.stdin.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    if ss[1] not in partition:
	partition[ ss[1] ] = Set([ ss[0] ])
	keys.append( ss[1] )
    else:
	partition[ ss[1] ].add(ss[0])

for i in range(len(keys)):
    txt = keys[i]
    rep = partition[txt].pop()
    sys.stdout.write( '{}\t{}\t{} '.format(rep, txt, rep) )
    for id_str in partition[txt]:
	sys.stdout.write( '{} '.format(id_str) )
    sys.stdout.write('\n')
