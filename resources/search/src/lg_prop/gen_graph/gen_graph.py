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

if len(sys.argv) != 6:
	exit ('Params: doc_fea_mat num_doc num_fea sigma output')

df_f = open(sys.argv[1])
num_doc = int(sys.argv[2])
num_fea = int(sys.argv[3])
sigma = float(sys.argv[4])
# ou_f = open(sys.argv[7], 'w')

# whole matrix
mat = lil_matrix((num_doc, num_fea))

# read in the doc_fea_mat
row_idx = 0
while True:
	line = df_f.readline()
	if not line:
		break
	ss = line.strip().split(' ')
	# sparse format: feat_id:feat_val
	
	for fea_val in ss:
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

# construct similarity matrix
norm_diag = lil_matrix((num_doc, num_doc))
affm = lil_matrix((num_doc, num_doc))
sys.stdout.write('similarity matrix...\n')
norms = (mat.multiply(mat)).sum(1)
norm_diag.setdiag( np.power(norms, -0.5) ) # inverse L2-norms
#print 'norm_diag', norm_diag
affm = norm_diag * mat * mat.transpose() * norm_diag
#print 'affm, cosine', affm

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
mat.dump(sys.argv[5])

