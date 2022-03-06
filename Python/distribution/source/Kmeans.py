from sklearn.cluster import KMeans
import numpy as np
import matplotlib.pyplot as plt

x = np.loadtxt('coordinates.txt', delimiter=' ')

kmeans = KMeans(n_clusters=50, random_state=0, n_jobs=-1).fit(x)
kmeans.labels_

kmeans.cluster_centers_

print("\nClusters Created")

plt.plot(kmeans.cluster_centers_[:,0], kmeans.cluster_centers_[:,1], 'ro', x[:,0], x[:,1], 'g^')
plt.axis([np.amin(x[:,0]), np.amax(x[:,0]), np.amin(x[:,1]), np.amax(x[:,1])])
#plt.axis([np.amin(kmeans.cluster_centers_[:,0]), np.amax(kmeans.cluster_centers_[:,0]), np.amin(kmeans.cluster_centers_[:,1]), np.amax(kmeans.cluster_centers_[:,1])])
plt.show()
