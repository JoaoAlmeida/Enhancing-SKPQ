import numpy as np
import matplotlib.pyplot as plt
import array as arr
import matplotlib.ticker as mtick

#x = np.loadtxt('coordinates.txt', delimiter=' ')
#dist = np.loadtxt('sequentialPOIs.txt', delimiter='\n')
#prob = np.loadtxt('paretoProbability.txt', delimiter='\n')

#bota esse
data = np.loadtxt('distances.txt', dtype={'names': ('distance', 'probability'), 'formats': ('f8', 'f8')})

x = arr.array('f', [0, 10, 100, 1000])
#dist_list = dist.tolist()

#dist_list.pop()

#possiveis pontos , ou .
f = plt.plot(data['distance'], data['probability'], 'b.')
plt.axis([0.0, 25000, np.amin(data['probability']), np.amax(data['probability'])])
#plt.xscale("log")
#plt.yscale("symlog")
plt.xlabel("Distance between region center and POI (m)")
plt.ylabel("Probability")

#colors = (0,0,0)
#area = np.pi*3

#plt.scatter(data['probability'], data['distance'], s=area, c=colors, alpha=0.5)
#plt.title('Scatter plot pythonspot.com')
#plt.xlabel('Distance')
#plt.ylabel('Probability')
#plt.show()

#scientfic notation?
#plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

#plt.show()
