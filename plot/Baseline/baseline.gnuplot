set terminal svg
set output "baseline.svg"
set multiplot layout 1, 3 title ""

set style data histogram
set style fill pattern border
set style histogram clustered gap 1
set datafile separator ","

set format x ""
set yrange [0:1]
set title "Precision"
plot "baseline.csv" using 1 title "Baseline on movies with IMDb-id", "baseline.csv" using 2 title "MFM on movies with IMDb-id", "baseline.csv" using 3 title "MFM on movies without IMDb-id"
set title "Recall"
set nokey
plot "baseline.csv" using 4 title "Baseline on movies with IMDb-id", "baseline.csv" using 5 title "MFM on movies with IMDb-id", "baseline.csv" using 6 title "MFM on movies without IMDb-id"
set title "F0.5"
set nokey
plot "baseline.csv" using 7 title "Baseline on movies with IMDb-id", "baseline.csv" using 8 title "MFM on movies with IMDb-id", "baseline.csv" using 9 title "MFM on movies without IMDb-id"
