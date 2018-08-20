# this python module will do the graph-based learning method 
# local-global propagation by Denny Zhou

# command line arguments
# @input	document-feature matrix, sparse vectors. doc_id (ftr_id:value)+
#			feature-feature edge lists, list of (f1,f2,value) tuples 
#			a file containing the labels, node_id:labels
# @output: a dictionary of node_id:label_value pairs

import sys
import numpy as np
from scipy.sparse import *
import math

if len(sys.argv) != 8:
	exit ('Params: doc_fea_mat num_doc num_fea num_cat labels alpha output')

df_f = open(sys.argv[1])
num_doc = int(sys.argv[2])
num_fea = int(sys.argv[3])
num_cat = int(sys.argv[4])
lb_f = open(sys.argv[5])
alpha = float(sys.argv[6])
ou_f = open(sys.argv[7], 'w')

# whole matrix
mat = lil_matrix((num_doc, num_fea))
tgt = lil_matrix((num_doc, num_cat))
res = lil_matrix((num_doc, num_cat))

# dat_id to row_idx
id_row = {}
ids = []

# read in the doc_fea_mat
row_idx = 0
while True:
	line = df_f.readline()
	if not line:
		break
	ss = line.strip().split(' ')
	doc_id = ss[0] # get the document id
	id_row[doc_id] = row_idx
	ids.append(doc_id)

	# sparse format: feat_id:feat_val
	
	for fea_val in ss[1:]:
		parts = fea_val.split(':')
		fea_id = int(parts[0]) - 1 # get the feature id
		val = float(parts[1])
		mat[row_idx, fea_id] = val
	row_idx += 1
	'''
	# dense format: feat1_val feat2_val
	fea_id = 0
	for fea_val in ss[1:]:
		val = float(fea_val)
		mat[doc_id, fea_id] = val
		fea_id += 1
	sys.stdout.write(str(doc_id) + '\r')
	'''
df_f.close()

# each line: f|d<id> <label>
while True:
	line = lb_f.readline()
	if not line:
		break
	[node, lbl] = line.strip().split('\t')
	# determine the node_id
	if node[0] in id_row:
		node_id = id_row[node[0]]
	else:
		exit(line + '\nDATA_ID not found in feature matrix')
	# determine the label
	if lbl == '1':
	    lbl_id = 1
	elif lbl == '-1':
	    lbl_id = 0
	tgt[node_id, lbl_id] = 1
	res[node_id, lbl_id] = 1 # res is initialized the same as tgt
lb_f.close()

# construct similarity matrix
norm_diag = lil_matrix((num_doc, num_doc))
affm = lil_matrix((num_doc, num_doc))
sys.stdout.write('similarity matrix...\n')
norms = (mat.multiply(mat)).sum(1)
norm_diag.setdiag( np.power(norms, -0.5) ) # inverse L2-norms
#print 'norm_diag', norm_diag
affm = norm_diag * mat * mat.transpose() * norm_diag
#print 'affm, cosine', affm

sigma = 0.15
affm.data[:] = 1 - affm.data
#print 'before d^2/sigma^2'
affm = affm.multiply(affm) / (2* (sigma**2))
#print 'before exponentiation'
affm.data[:] = np.exp( - affm.data )
#print 'before setting zero'
affm.setdiag( np.zeros(num_doc) )
#print 'affm, diag zero', affm
mat = affm

# print mat
# normalize the similarity matrix
sys.stdout.write('normalizing similarity matrix...\n')
norms = mat.sum(1)
for i in range(num_doc):
	norm_diag[i, i] = 1 / math.sqrt(norms[i, 0])
mat = norm_diag * mat * norm_diag

# the propagation starts here
sys.stdout.write('start propagation...\n')
ITERATION = 1
for i in range(ITERATION):
	sys.stdout.write('.')
	sys.stdout.flush()
	res = alpha * mat * res + (1 - alpha) * tgt
sys.stdout.write('\n')

# output the result
for i in range(num_doc):
	ou_f.write('{}\t'.format(ids[i]))
	ou_f.write('{}\t'.format( int(np.argmax(res[i,:].todense())*2-1) ))
	for t in range(num_cat):
		ou_f.write('{}\t'.format(res[i, t]))
	ou_f.write('\n')
ou_f.close()
