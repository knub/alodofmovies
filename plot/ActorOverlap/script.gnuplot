set terminal svg
set output "match-with-varying-actor-overlap-2014-07-06.svg"
set object 1 rectangle from screen 0,0 to screen 1,1 fillcolor rgb"#FFFFFF" behind 
set style data lines
set title "Parameter evaluation for ACTOR_OVERLAP_MINIMUM"
set datafile separator "\t"
set palette
set key autotitle columnhead
plot "match-with-varying-actor-overlap-2014-07-06.tsv" using 1:2 title "precision", \
"match-with-varying-actor-overlap-2014-07-06.tsv" using 1:3 title "recall", \
"match-with-varying-actor-overlap-2014-07-06.tsv" using 1:4 title "f1-measure", \
"match-with-varying-actor-overlap-2014-07-06.tsv" using 1:5 title "f0.5-measure"

