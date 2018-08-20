import sys

if len(sys.argv) != 5:
        exit('Usage: vocab_list stop_list length_threshold output_list')

vo_f = open(sys.argv[1])
st_f = open(sys.argv[2])
max_len = int(sys.argv[3])
ou_f = open(sys.argv[4], 'w')

def is_ascii(s):
        try:
                s.decode('ascii')
        except UnicodeDecodeError:
                return False
        else:
                return True
 
stopwords = []
for line in st_f.readlines():
    w = line.strip()
    stopwords.append(w)

while True:
        line = vo_f.readline()
        if not line:
                break
        ss = line.strip().split('\t')
	if not is_ascii(ss[0]):
	    continue
	tt = ss[0].split(' ')
	stop_count = 0
	max_count = 0
	for s in tt:
	    if s in stopwords:
		stop_count += 1
	    if len(s) >= max_len:
		max_count += 1
	if stop_count == len(tt) or max_count > 0:
	    continue
	ou_f.write(ss[0] + '\t' + ss[1] + '\n')

vo_f.close()
st_f.close()
ou_f.close()
