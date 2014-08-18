set terminal svg
set output "candidateSetSize.svg"
set multiplot layout 3, 1 title "Candidate set size evaluation with respect to the evaluation metrics"

set datafile separator ","
set logscale x 2
set xlabel "# of candidates considered for a movie"
set style data lines
set ylabel "Precision"
plot "candidateSetSize.csv" using 1:2 with lines title ""
set ylabel "Recall"
plot "candidateSetSize.csv" using 1:3 with lines title ""
set ylabel "F0.5"
plot "candidateSetSize.csv" using 1:5 with lines title ""
