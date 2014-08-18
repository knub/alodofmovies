set terminal svg size 800,700
set output "roc.svg"
set xrange [0:1]
set yrange [0:1]
set xlabel "False positive rate"
set ylabel "True positive rate"
set key right bottom
set datafile separator ","
plot 'roc_out.csv' using 3:2 title 'ROC curve' pt 7, \
	 x title 'Random guess', \
     'roc_out.csv' using 3:2:1 with labels offset 3, 0, 5, 0 notitle
