set terminal wxt persist
set style data lines
set title "Parameter evaluation for ACTOR_OVERLAP_MINIMUM"
set datafile separator ","
set pm3d
set palette
set key autotitle columnhead
set xlabel "MIN_SCORE"
set ylabel "ACT_DIST"
set zlabel "F0.5"
splot "3d-plot-2014-08-12.csv" using 1:2:5 with lines title columnhead
