#!/bin/bash

feb="/storage2/foreseer/twitter/gardenhose/processed/2013/2"
mar="/storage2/foreseer/twitter/gardenhose/processed/2013/3"
reformater="/storage3/users/raywang/large/src/reformat"
rcd='../data/timestamp.txt'

for f in `find $feb -name "*fmt*"`
do
    echo $f
    cat $f | $reformater | head -n1 >> $rcd
done

for f in `find $mar -name "*fmt*"`
do
    echo $f
    cat $f | $reformater | head -n1 >> $rcd
done
