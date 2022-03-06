import numpy as np
import matplotlib.pyplot as plt
import array

ndcg = np.array(["NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"])
influence = np.array([0.89, 0.88, 0.88, 0.87])
skpq = np.array([0.79, 0.80, 0.80, 0.79])
p_rank = np.array([0.67, 0.69, 0.67, 0.68])
p_search = np.array([0.92, 0.90, 0.89, 0.89])

#possiveis pontos: , ou .
#f = plt.plot(data['lgt'], data['lat'], 'bo')


#plt.xscale("log")
#plt.yscale("symlog")

#plt.subplot(1, 1, 1)
plt.plot(ndcg, skpq,'r:', lw=1, marker="^", markersize=10)
plt.text(1,skpq[1]+0.005,skpq[1])
plt.plot(ndcg, influence,'k-.', lw=1, marker="*", markersize=10)
plt.plot(ndcg, p_rank,'g--', lw=1, marker="o", markersize=10)
plt.plot(ndcg, p_search,'b-', lw=1, marker="s", markersize=10)
plt.grid(True)

#plt.scatter(ndcg,skpq,marker='o', color='midnightblue')
plt.yticks((0.6, 0.7,0.8,0.9), color='k')
#plt.axis(v)
#plt.xlabel("longitude")
#plt.ylabel("NDCG")
plt.show()