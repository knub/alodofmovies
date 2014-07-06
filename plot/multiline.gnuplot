set terminal svg
set output "multiline.svg"
set style data lines
set title "Parameter evaluation for ACTOR_OVERLAP_MINIMUM"
set datafile separator "\t"
set pm3d
set palette
set key autotitle columnhead
plot "2014-07-06_01.tsv" using 2:3 title "precision", \
"2014-07-06_01.tsv" using 2:4 title "recall", \
"2014-07-06_01.tsv" using 2:5 title "f1-measure"