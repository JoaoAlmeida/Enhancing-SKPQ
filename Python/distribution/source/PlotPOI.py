import numpy as np
import matplotlib.pyplot as plt
import array as arr

data = np.loadtxt('./files/LondonLGD.txt', delimiter=' ', usecols=(0,1),dtype={'names': ('lat','lgt'), 'formats': ('f8', 'f8')})

#possiveis pontos: , ou .
#f = plt.plot(data['lgt'], data['lat'], 'bo')


#plt.xscale("log")
#plt.yscale("symlog")



plt.scatter(data['lgt'],data['lat'],marker='o', color='midnightblue')
#v=[np.amin(data['lgt'])-0.05, np.amax(data['lgt'])+0.05, np.amin(data['lat'])-0.05, np.amax(data['lat']+0.05)]
#plt.axis(v)
plt.xlabel("longitude")
plt.ylabel("latitude")
plt.show()