import matplotlib.pyplot as plt
import sys
import re

# Script that generates plots about buffer occupancy from buffer occupancy report
# Takes as arguments
# (1) a buffer occupancy report
# (2) path to save the graphic to.
#
# The buffer occupancy report file should have a format like:
# [Simulation time] [average buffer occupancy % [0..100] ] [variance] [min] [max]

# Read delay analysis from file
with open(sys.argv[1]) as file_name:
    analysis = file_name.readlines()

times = []
avgOccupancies = []
minima = []
maxima = []

for line in analysis:
    match = re.match("(\d+\.\d+\s)(\d+\.\d+\s)(\d+\.\d+\s)(\d+\.\d+\s)(\d+\.\d+)", line)
    if match is None:
        print("no match")
        continue
    simTime = float(match.group(1)) / 60
    avgOccupancy = float(match.group(2))
    variance = float(match.group(3))
    min = float(match.group(4))
    max = float(match.group(5))
    times.append(simTime)
    avgOccupancies.append(avgOccupancy)
    minima.append(min)
    maxima.append(max)

#Turn resulution high enough for high quality image
plt.style.use('ggplot')
plt.rcParams['savefig.dpi'] = 300

plt.plot(times, avgOccupancies)
plt.xlabel('Buffer Occupancy')
plt.ylabel('Cumulative percentage')
plt.grid(True)

plt.savefig(sys.argv[2])