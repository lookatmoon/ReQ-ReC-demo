import sys

if len(sys.argv) < 2:
    exit ('Params: [file weight]+')

i = 1
n = (len(sys.argv) - 1) / 2
for k in range(n):
    in_f = open(sys.argv[2*k+1])
    weig = float(sys.argv[2*k+2])
    while True:
	line = in_f.readline()
	if not line:
	    break
	sys.stdout.write('{}:{}\n'.format(i, weig))
	i += 1
    in_f.close()
