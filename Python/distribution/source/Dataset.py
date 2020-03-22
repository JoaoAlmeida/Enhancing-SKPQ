import numpy as np
import matplotlib.pyplot as plt
import array as arr
import matplotlib.ticker as mtick

#x = np.loadtxt('coordinates.txt', delimiter=' ')
dist = np.loadtxt('sequentialPOIs.txt', delimiter='\n')
prob = np.loadtxt('paretoProbability.txt', delimiter='\n')

x = arr.array('f', [0, 10, 100, 1000])
dist_list = dist.tolist()

dist_list.pop()

plt.plot(dist, prob, 'ro')
plt.axis([0.0, 25000, np.amin(prob), np.amax(prob)])
plt.xlabel("Distance between region center and POI (m)")
plt.ylabel("Probability")

#scientfic notation?
#plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

plt.show()
