set terminal svg font "CMU Serif Roman,10"
set output "weight.svg"
set multiplot layout 3, 1 title "Different weights"

set datafile separator ","
set style data lines
set title "Precision"
plot "weight.csv" using 1:2 with lines title ""
set title "Recall"
plot "weight.csv" using 1:3 with lines title ""
set title "F0.5"
plot "weight.csv" using 1:5 with lines title ""
