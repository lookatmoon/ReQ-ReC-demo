import re
import sys

if len(sys.argv) != 2:
    exit ('Param: text_field_id')
field_id = int(sys.argv[1])

while True:
    line = sys.stdin.readline()
    if not line:
	break
    ss = line.strip().split('\t')
    txt = ss[field_id]
    m = re.search(r'^.{0,8}RT', txt)
    if m == None:
	sys.stdout.write(line)

