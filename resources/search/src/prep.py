import sys
import re
#from dateutil import parser

# global variables
punct = '.,:;~`@^&*()+={}[]|\\"<>/' # not including: ' ! ? # -

def process_text(t):
	#padding
	t = t + ' '

	#handle the non-ASCII prime ' 
	t = t.replace('\xe2\x80\x99', '\'')
	t = re.sub(r'\xe2..', '', t)
	
	#remove urls
	t = re.sub(r'http[^ ]+', ' http_url ', t)
	
	#remove non-english characters
	s = list(t)
	for i in range(len(s)):
	    if ord(s[i]) > 128:
		s[i] = ''
	t = ''.join(s)

	#remove tail hashtags
	# t = re.sub(r'(#[_a-zA-Z0-9]+\s+)+$', '', t + ' ')

	#replace numbers
	t = re.sub(r' [-+]?[0-9]*\.?[0-9]+ ', ' -number- ', t)
	
	#replace dollars
	# t = re.sub(r'\$[-+]?[0-9]*\.?[0-9]+', ' dollar_number ', t)
	# t = re.sub(r'[-+]?[0-9]*\.?[0-9]+%', ' number_percent ', t)
	
	#handle ', 's
	t = re.sub(r'\'s\s', ' ', t)
	t = t.replace('\'', '')

	#expand puncts
	for p in punct:
		t = t.replace(p, ' ')
	
	#t = t.replace('!number!', '-number-')

	t = t.replace('?', ' ? ')
	t = t.replace('!', ' ! ')

	#reduce spaces
	t = re.sub(r'\s+', ' ', t)
	
	#lowercase
	t = t.lower()
        
        # trim
        t = t.strip()

	return t
	
def process_htag(t):
	t = re.sub(r'\s+', ' ', t)
	t = t.lower().strip()
	return t
	
def process_time(t):
	t = parser.parse(t) # parse time string into datetime struct
	
### main program
# argument filter
#if len(sys.argv) != 3:
#	exit('Please provide TWO arguments: input_file_list output_file_list')

#inf = open(sys.argv[1])
#ouf = open(sys.argv[2], 'w')

# line format: 	line = id + '\t' + txt + '\t' + ht + '\t' + time + '\t' + loc;

while True:
	line = sys.stdin.readline()
	if not line:
		break
	#ss = line.strip().split('\t')
	line = line.strip()
	line = process_text(line)
	# ss[3] = process_time(ss[3])
	# ss[4] = process_loca(ss[4])
	#newl = '\t'.join(ss) + '\n'
	# print ss[1]
	#ouf.write(newl)
	sys.stdout.write(line + '\n')

#inf.close()
#ouf.close()
