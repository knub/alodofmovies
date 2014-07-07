import difflib
import sys
from os import listdir

def writeDiff(fileList, outputName):
	f = open(outputName,'w')
	f.write("day,plus,minus\n")

	for i in range(len(fileList) - 1):
		diff = difflib.ndiff(open(fileList[i]).readlines(), open(fileList[i + 1]).readlines())

		plus = 0
		minus = 0

		for line in diff:
			if line.startswith("+"):
				plus += 1
			elif line.startswith("-"):
				minus += 1

		f.write("{0},{1},{2}\n".format(i, plus, minus))

	f.close()



def main():
	files = listdir("/home/tanja/Desktop/IMDB/")

	upcomingFiles = []
	movieFiles = []

	for f in files:
		if f.startswith("2014"):
			upcomingFiles.append(f)
		elif f.startswith("tt"):
			movieFiles.append(f)

	upcomingFiles = sorted(upcomingFiles)
	writeDiff(upcomingFiles, "upcomingMovies.csv")
	movieFiles = sorted(movieFiles)
	writeDiff(movieFiles, "newMovie.csv")



if __name__ == "__main__":
    main()