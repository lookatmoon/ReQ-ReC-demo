#!/bin/bash

timestamp=`date +"%m%d_%H%M%S"`
if [ $# -ne 3 ]; then
    echo 'Params: qid quoted_query num_results'
    exit 1
fi

# switch to current folder
base_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $base_dir

qid=$1
query=$2
num_res=$3
index='../../../fulltext/index'

data_dir='../results/'$qid
if [ -d $data_dir ]; then
    echo $data_dir 'exist, rewrite.'
else
    echo $data_dir 'does not exist, create'
    mkdir $data_dir
    chmod 777 $data_dir
fi
query_file=$data_dir'/queries.txt'

file_name=$data_dir'/'$qid'.tsv'

java -jar search.jar -index $index -query "$query" -max $num_res -out "$qid;docID;text" > $file_name

printf "$timestamp\t$qid\t$query\t$num_res\t$index\n" >> $query_file

echo 'search results are saved to' $file_name

# cd back
cd -
