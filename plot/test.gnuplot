set style data lines
set title "Parameter evaluation for ACTOR_OVERLAP_MINIMUM"
set datafile separator ","
set pm3d
set palette
splot "testdata.csv" using 1:2:3
