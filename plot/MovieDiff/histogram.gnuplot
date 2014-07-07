clear
reset
unset key

set terminal svg
set output "newMovie.svg"
# Make the x axis labels easier to read.
set xtics rotate out
set xlabel "Days"
set ylabel "# of triples"
# Select histogram data
set style data histogram
# Give the bars a plain fill pattern, and draw a solid line around them.
set style fill solid border
set style histogram clustered
set datafile separator ","
plot for [COL=2:3] 'newMovie.csv' using COL:xticlabels(1) title columnheader
