import numpy as np
import matplotlib.pyplot as plt
import array as arr
import matplotlib.ticker as mtick

#x = np.loadtxt('coordinates.txt', delimiter=' ')
#dist = np.loadtxt('sequentialPOIs.txt', delimiter='\n')
#prob = np.loadtxt('paretoProbability.txt', delimiter='\n')

#bota esse
data = np.loadtxt('./files/distancesNewYork.txt', dtype={'names': ('distance', 'probability'), 'formats': ('f8', 'f8')})

x = arr.array('f', [0, 10, 100, 1000])
#dist_list = dist.tolist()

#dist_list.pop()
data['distance'] = data['distance'] / 1000
#data['probability'] = data['probability'] / 1000
#possiveis pontos , ou .
plt.rcParams.update({'font.size': 50, "axes.labelsize": "x-large"})
f = plt.plot(data['distance'], data['probability'], 'b.')
plt.axis([0.0, 25, np.amin(data['probability']), np.amax(data['probability'])])
#plt.xscale("log")
#plt.yscale("symlog")

plt.xlabel("Distance (Km)",  fontsize=50)
#plt.xlabel("Distance between POI location and features (km) in its spatial neighborhood")
plt.ylabel("Probability", fontsize=50)



#Ficou feio o scatter
#plt.scatter(data['distance'], data['probability'], marker='o', color='blue', edgecolors='black')
#plt.axis([0.0, 1000, np.amin(data['probability']), np.amax(data['probability'])])
#plt.title('Scatter plot pythonspot.com')
#plt.xlabel('Distance (km)')
#plt.ylabel('Probability')
#plt.show()

#scientfic notation?
#plt.ticklabel_format(style='sci', axis='y', scilimits=(0,0))

#plt.show()
