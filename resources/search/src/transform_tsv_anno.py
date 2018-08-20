import sys

while True:
    line = sys.stdin.readline()
    if not line:
	break
    ss  = line.strip().split('\t')
    sys.stdout.write('{}\t{}\t{}\n'.format(0, ss[1], ss[2]))
    
