import sys

dat = []

def get_start_end(s, start, end):
    start_pos = s.find(start) + len(start)
    end_pos = s.find(end)
    return s[start_pos:end_pos]

while True:
    line = sys.stdin.readline()
    if not line:
	break
    if not line.startswith('#'): # one status is available
    	ss  = line.strip().split(' ')
	qid = ss[0]
	tid = ss[2]
	rnk = ss[3]
	sco = ss[4]
	tag = ss[5]
    	line = sys.stdin.readline()
	usr = get_start_end(line, ', screen_name:', ', epoch:')
	epo = get_start_end(line, ', epoch:', ', text:')
	txt = get_start_end(line, ', text:', ', followers_count:')
	fol = get_start_end(line, ', followers_count:', ', statuses_count:')
	cnt = get_start_end(line, ', statuses_count:', ', lang:')
	lng = get_start_end(line, ', lang:', ', in_reply_to_status_id:')
	rep_twt = get_start_end(line, ', in_reply_to_status_id:', ', in_reply_to_user_id:')
	rep_usr = get_start_end(line, ', in_reply_to_user_id:', ', retweeted_status_id:')
	rt_twt = get_start_end(line, ', retweeted_status_id:', ', retweeted_user_id:')
	rt_usr = get_start_end(line, ', retweeted_user_id:', ', retweeted_count:')
	rt_cnt = get_start_end(line, ', retweeted_count:', ')\n')
	print '{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}'.format(0, tid, txt, qid, rnk, sco, tag, usr, epo, txt, fol, cnt, lng, rep_twt, rep_usr, rt_twt, rt_usr, rt_cnt)


	
    else:
	continue
    
