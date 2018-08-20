#!/bin/bash

if [ $# -ne 2 ]; then
    echo 'Params: tsv_file processed_file'
    exit 1
fi

src_dir=/storage4/foreseer/users/raywang/annoloop/src
infile=$1
oufile=$2
indir=$(dirname $infile)
partition=$indir/partition.txt

cut -f2,8 $infile | python $src_dir/prep_clean.py | python $src_dir/dedup_exact.py > $partition 
cut -f1,2 $partition > $oufile

