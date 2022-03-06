from sklearn.cluster import KMeans
import numpy as np
from sklearn.datasets import make_blobs
from sklearn.metrics import silhouette_score
import matplotlib.pyplot as plt
import numpy as np

x = np.loadtxt('coordinates.txt', delimiter=' ')

sil = []
kmax = 51
# x, y = make_blobs(n_samples = 1000, centers = 3, n_features=2, shuffle=True, random_state=31)

# dissimilarity would not be defined for a single cluster, thus, minimum number of clusters should be 2
for k in range(30, kmax+1):
  kmeans = KMeans(n_clusters = k).fit(x)
  labels = kmeans.labels_
  sil.append(silhouette_score(x, labels, metric = 'euclidean'))

x = range(30,52)

plt.plot(x, sil, label='silhouette')

# Add a legend
plt.legend()

# Show the plot
plt.show()
