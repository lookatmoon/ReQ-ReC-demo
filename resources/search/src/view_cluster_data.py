import sys
from sets import Set
# import codecs

if len(sys.argv) != 4:
    exit ('Params: text_data data_map output')

txt_f = open(sys.argv[1])
# txt_f = codecs.open(sys.argv[1],'r', 'utf-8', 'strict')
map_f = open(sys.argv[2])
# out_f = codecs.open(sys.argv[3],'w', 'utf-8', 'strict')
out_f = open(sys.argv[3], 'w')


# read in the map: original_data_id -> cluster_data_id
cid = Set([])
while True:
    line = map_f.readline()
    if not line:
	break
    ss = line.strip().split(':')
    cid.add( int(ss[0]) )
map_f.close()

ln = 1
while True:
    line = txt_f.readline()
    if not line:
	break
    if ln in cid:
	out_f.write(line)
    ln += 1
txt_f.close()
out_f.close()
    
