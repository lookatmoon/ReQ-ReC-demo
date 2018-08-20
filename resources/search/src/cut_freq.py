import sys

if len(sys.argv) != 2:
    exit ('Params: threshold')

threshold = int(sys.argv[1])

ln = 1
while True:
    line = sys.stdin.readline()
    if not line:
	break
    freq = int(line.strip().split('\t')[1])
    if freq < threshold:
	sys.stdout.write('{}\n'.format(ln - 1))
	break
    ln += 1
