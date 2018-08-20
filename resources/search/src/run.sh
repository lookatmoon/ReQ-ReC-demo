#!/bin/bash
timestamp=`date +"%m%d_%H%M%S"`

if [ $# -ne 3 ]; then
    echo 'Params: query_id num_clusters(0: no clustering) top_k(used when num_clusters > 0)'
    exit 1
fi

# switch to current folder
base_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $base_dir

prefix='..'

data_dir=$prefix'/results/'$1
# data_dir='../'$1
if [ -d $data_dir ]; then
    echo $data_dir 'exists. Overwriting...'
else
    echo $data_dir 'does not exist!'
    exit 1
fi
k_comp=$2
top_prf=$3

src_dir=$prefix'/src'

# data_dir='../data'
# data_dir='../water'
input_dir=$prefix'/input'
# input_dir='../input'
data_txt=$data_dir'/'$1'.txt'
data_tsv=$data_dir'/'$1'.tsv'

# API ADAPTATION
#cp $data_txt $data_tsv

clean_tsv=$data_dir'/'$1'_clean.tsv'

norm_txt=$data_dir'/norm.txt'
norm_vec=$data_dir'/norm.vec'
map=$data_dir'/map'
norm_ne_vec=$data_dir'/norm_ne.vec'
norm_rw_vec=$data_dir'/norm_rw.vec'
unigram_sort=$data_dir'/unigram_sort.freq'
bigram_sort=$data_dir'/bigram_sort.freq'
trigram_sort=$data_dir'/trigram_sort.freq'
stop_list=$input_dir'/stop.txt'
max_word_len=20
skip_ngram_window=8
unigram_filter=$data_dir'/unigram_filter.freq'
bigram_filter=$data_dir'/bigram_filter.freq'
trigram_filter=$data_dir'/trigram_filter.freq'
feature_list=$data_dir'/feature.lst'
feat_weight=$data_dir'/feat_weight'
anno=$data_dir'/annotate.txt'
partition=$data_dir'/partition_'$timestamp'.txt'

text2wfreq='python '$prefix'/src/gen_unigram.py'
text2ngramfreq='python '$prefix'/src/gen_ngram.py'
text2skngramfreq='python '$prefix'/src/gen_skip_ngram.py'

#cp $data_dir'/'$1'.txt' $data_dir'/'$1'_'$timestamp'.txt'

if [ $k_comp -eq 0 ]; then
    echo 'no clustering, just reformat the data'
    
    # basic cleaning of the data: deduplication, non-English
#    python $src_dir/transform.py < $data_txt > $data_tsv
    echo 'filtering duplicates, non English tweets...'
    python $src_dir/prep_clean.py < $data_tsv > $clean_tsv # text cleaned
    
    python $src_dir/transform_tsv_anno.py < $clean_tsv > $anno
    exit 0
fi

# transform the list from API to tab-separated-format
#python $src_dir/transform.py < $data_txt > $data_tsv

# basic cleaning of the data: deduplication, non-English
echo 'filtering duplicates, non English tweets...'
python $src_dir/prep_clean.py < $data_tsv > $clean_tsv # text cleaned

# preprocess the data
# cut -f8 $data_tsv | python $src_dir/prep.py > $norm_txt
cut -f3 $clean_tsv | python $src_dir/prep.py > $norm_txt
echo 'sorting unigrams...'
cat $norm_txt | $text2wfreq | sort -nr -k2 > $unigram_sort
echo 'sorting bigrams...'
cat $norm_txt | $text2ngramfreq $unigram_sort 2 | sort -nr -k3 > $bigram_sort
# cat $norm_txt | $text2skngramfreq $unigram_sort $skip_ngram_window 2 | sort -nr -k3 > $bigram_sort
echo 'sorting trigrams...'
cat $norm_txt | $text2ngramfreq $unigram_sort 3 | sort -nr -k4 > $trigram_sort
# cat $norm_txt | $text2skngramfreq $unigram_sort $skip_ngram_window 3 | sort -nr -k4 > $trigram_sort

echo 'cutting off ...'
unigram_cut=$data_dir'/unigram_cut.freq'
bigram_cut=$data_dir'/bigram_cut.freq'
trigram_cut=$data_dir'/trigram_cut.freq'
cut_freq=$src_dir/cut_freq.py

cut_off=4
num_line=`python $cut_freq $cut_off < $unigram_sort`
head -n $num_line $unigram_sort > $unigram_cut

cut_off=4
num_line=`python $cut_freq $cut_off < $bigram_sort`
head -n $num_line $bigram_sort > $bigram_cut

cut_off=4
num_line=`python $cut_freq $cut_off < $trigram_sort`
head -n $num_line $trigram_sort > $trigram_cut

echo 'filtering out stopwords from unigrams'
python $src_dir/filter_vocab.py $unigram_cut $stop_list $max_word_len $unigram_filter
python $src_dir/filter_vocab.py $bigram_cut  $stop_list $max_word_len $bigram_filter
python $src_dir/filter_vocab.py $trigram_cut $stop_list $max_word_len $trigram_filter

mv $unigram_filter $unigram_cut
mv $bigram_filter $bigram_cut
mv $trigram_filter $trigram_cut

cat $unigram_cut $bigram_cut $trigram_cut > $feature_list
# cat $bigram_cut $trigram_cut > $feature_list
num_feature="`wc -l $feature_list| awk '{print $1}'`"

echo 'vectorizing...'
vectorizer='python '$prefix'/src/gen_ngram_vector.py'
cat $norm_txt | $vectorizer TRUE "1,2,3" $unigram_cut $bigram_cut $trigram_cut > $norm_vec

# vectorizer='python /storage3/users/raywang/ngram/src/gen_skip_ngram_vector.py'
# cat $norm_txt | $vectorizer TRUE $skip_ngram_window "1,2,3" $unigram_cut $bigram_cut $trigram_cut > $norm_vec

# remove empty lines
python $src_dir/rm_empty_get_map.py < $norm_vec $map > $norm_ne_vec
num_data="`wc -l $norm_ne_vec | awk '{print $1}'`"

# python $src_dir/gen_feat_weight.py $unigram_cut 1 $bigram_cut 1.414 $trigram_cut 1.732 > $feat_weight
# python $src_dir/gen_feat_weight.py $unigram_cut 1 $bigram_cut 1 $trigram_cut 1 > $feat_weight
# python $src_dir/gen_feat_weight.py $unigram_cut 1 $bigram_cut 2 $trigram_cut 3 > $feat_weight
# python $src_dir/reweight_vectors.py $feat_weight < $norm_ne_vec > $norm_rw_vec

# clustering
# set the dynamic library path
if [ "$(uname)" == "Darwin" ]; then
    export DYLD_LIBRARY_PATH=${DYLD_LIBRARY_PATH}:$base_dir'/../../clustering/arma'
elif [ "$(uname)" == "Linux" ]; then
    export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$base_dir'/../../clustering/arma'
    echo $LD_LIBRARY_PATH >> $feature_list
fi
clustering='../../clustering/clustering'
model=$data_dir'/model'
result=$data_dir'/result'
twice_k_comp=`echo $k_comp*2|bc`
if [[ $twice_k_comp -ge $num_data ]]; then
    # too few data, random sampling
    smp=$data_dir/sample
    seq 1 $num_data | python $src_dir/reservoir_sample.py $k_comp > $smp
    for i in `seq 1 $num_data`; do printf "1\t1\n" ; done > $result  
    i=1
    for l in `cat $smp`; do printf "$i\t$l\t1\t1\t1:1\n"; i=`echo $i+1|bc` ; done > $model
    rm $smp
else
    $clustering k_mean $norm_ne_vec $num_data $num_feature $k_comp $model $result
fi

#clustering='/storage3/users/raywang/kiva/cluster/clustering.sh'
#$clustering k_mean $norm_rw_vec $num_data $num_feature $k_comp $model $result

# view the results
top_feature=$data_dir'/top_feature'
python $src_dir/view_cluster_feature.py $feature_list $model 1 $top_feature

# cut -f8 $data_tsv > $data_dir/itmp
cut -f3 $clean_tsv > $data_dir/itmp
python $src_dir/view_cluster_data.py $data_dir/itmp       $map $data_dir/itmp1
python $src_dir/view_cluster_data.py $norm_txt  $map $data_dir/itmp2
assn=$data_dir'/assign'
paste $result $data_dir/itmp1 $data_dir/itmp2 > $assn
# paste $result $data_dir/itmp1  > $assn

for i in `seq 1 $k_comp`
do
    awk '{if ($1 == "'$i'") print $0;}' $assn | sort -k2 -rn > $data_dir'/c'$i
done

# python $src_dir/gen_anno.py $data_tsv $model $map $anno
python $src_dir/gen_anno.py $clean_tsv $model $map $top_prf $anno

rm $data_dir/itmp*

# cd back
cd -
