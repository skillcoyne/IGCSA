#!/bin/bash -l

dir=`pwd`

scripts=(variation-table.R gc-bin-tables.R variation-size-prob.R snv-prob.R)

for s in ${scripts[@]} ; do
	echo "Running $s"

	Rscript $dir/$s

	if [[ $? != 0 ]] ; then
  	echo "$s failed to run."
  	exit $?
	fi

done






 
