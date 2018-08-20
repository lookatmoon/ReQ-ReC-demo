import sys

score = 1200
while True:
    line = sys.stdin.readline()
    if not line:
	break
    tt = line.strip().split('\t')
    txt = tt[1]
    ss = tt[0].split(' ')
    sys.stdout.write('{} {} {} {} {} {}\t{}\n'.format(ss[0], ss[1], ss[2], ss[3], score, ss[5], txt))
    score -= 1
