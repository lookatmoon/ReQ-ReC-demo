import sys

if len(sys.argv) != 4:
    exit ('Params: id_set lines output')

id_f = open(sys.argv[1])
ln_f = open(sys.argv[2])
ou_f = open(sys.argv[3], 'w')

# first, read in the lines
ln = {}
while True:
    line = ln_f.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    ln[ ss[1] ] = line
ln_f.close()

# then read the ids and write out the lines
while True:
    line = id_f.readline()
    if not line:
	break
    id_str = line.strip()
    if id_str in ln:
	ou_f.write(ln[id_str])
id_f.close()
ou_f.close()
