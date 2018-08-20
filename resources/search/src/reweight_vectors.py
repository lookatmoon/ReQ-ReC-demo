import sys

if len(sys.argv) != 2:
    exit ('Params: id_weight_list')

wgt_f = open(sys.argv[1])

# load the weight list
id_wgt = {}
while True:
    line = wgt_f.readline()
    if not line:
	break
    ss = line.strip().split(':')
    id_wgt[ int(ss[0]) ] = float(ss[1])
wgt_f.close()

# change the incoming feature vectors
while True:
    line = sys.stdin.readline()
    if not line:
	break
    ss = line.strip().split(' ')
    for s in ss:
	fv = s.split(':')
	fid = int(fv[0])
	val = float(fv[1])
	try: val *= id_wgt[fid]
	except: pass
	sys.stdout.write('{}:{} '.format(fid, val))
    sys.stdout.write('\n')
	
