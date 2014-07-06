set terminal wxt persist
set style data lines
set title "Parameter evaluation for ACTOR_OVERLAP_MINIMUM"
set datafile separator "\t"
set pm3d
set palette
set key autotitle columnhead
splot "2014-07-06_01.tsv" using 1:2:5 w lines title columnhead
