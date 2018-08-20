import sys

if len(sys.argv) != 2:
    exit ('Param: in_out_map')

ou_f = open(sys.argv[1], 'w')

in_id = 1
ou_id = 1
while True:
    line = sys.stdin.readline()
    if not line:
	break
    if len(line.strip()) != 0:
	sys.stdout.write(line)
	ou_f.write('{}:{}\n'.format(in_id, ou_id))
	ou_id += 1
    in_id += 1

ou_f.close()
