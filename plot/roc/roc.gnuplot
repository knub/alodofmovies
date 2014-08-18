set terminal wxt size 800,700
set xrange [0:1]
set yrange [0:1]
set key right bottom
set datafile separator ","
plot 'roc.csv' using 2:3 title 'ROC curve' pt 7
