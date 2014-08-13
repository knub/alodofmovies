set terminal svg
set output "baseline.svg"
set multiplot layout 1, 3 title ""

set style data histogram
set style fill solid border
set style histogram clustered gap 1
set datafile separator ","

set format x ""
set yrange [0:1]
set title "Precision"
plot "baseline.csv" using 1 title "Baseline", "baseline.csv" using 2 title "MFM"
set title "Recall"
set nokey
plot "baseline.csv" using 3 title "Baseline", "baseline.csv" using 4 title "MFM"
set title "F0.5"
set nokey
plot "baseline.csv" using 5 title "Baseline", "baseline.csv" using 6 title "MFM"
