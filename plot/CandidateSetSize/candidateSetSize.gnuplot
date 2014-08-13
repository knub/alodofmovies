set terminal svg
set output "candidateSetSize.svg"
set multiplot layout 3, 1 title "Different weights"

set datafile separator ","
set style data lines
set title "Precision"
plot "candidateSetSize.csv" using 1:2 with lines title ""
set title "Recall"
plot "candidateSetSize.csv" using 1:3 with lines title ""
set title "F0.5"
plot "candidateSetSize.csv" using 1:5 with lines title ""
