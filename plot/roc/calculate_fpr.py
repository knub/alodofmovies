import csv

in_file = "/home/tanja/Repositories/alodofmovies/plot/roc/roc.csv"
out_file = "/home/tanja/Repositories/alodofmovies/plot/roc/roc_out.csv"

tp_fn = 2008
tn = 32

def calculate_tp(recall):
	return recall * 2008.0

def calculate_fp(precision, recall):
	tp = calculate_tp(recall)
	return (tp - (precision * tp)) / precision

def calculate_fpr(precision, recall):
	fp = calculate_fp(precision, recall)
	return fp / (fp + tn)


with open(in_file, 'rb') as input_file:
	with open(out_file, 'wb') as output_file:
		reader = csv.reader(input_file, delimiter = ',')
		writer = csv.writer(output_file, delimiter = ',')

		reader.next()
		writer.writerow(["threshold", "recall", "fpr"])

		for row in reader:
			threshold = float(row[0])
			precision = float(row[1])
			recall = float(row[2])
			fpr = calculate_fpr(precision, recall)

			writer.writerow([threshold, recall, fpr])