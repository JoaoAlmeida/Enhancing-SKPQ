import scipy.stats as stats
import numpy as np

#Berlin
kw_vec = ["amenity","natural","shop","bench","tourism","bicycle","information","waste","parking","berliner",
          "district","software","wedding","herb","door","pen","pension","development","resource","eagle"]
query = 'ParetoSearch-LD'
#query = 'SKPQ-LD'

k_vec=[5,10,15,20]
#k_vec=[5]

for kw in kw_vec:
      print("\n========== KEY:"+ kw.upper() +" ==========\n")
      for k in k_vec:
            data = np.loadtxt('./evaluator/'+query+' [k='+str(k)+', kw='+ kw + '].txt', delimiter='score=', usecols=1,dtype=str)
            data = [w.replace(']', '') for w in data]
            score = np.delete(data, range(1, len(data), 2), axis=0)

            rate = np.loadtxt('./evaluator/thrash/' + query + ' [k=' + str(k) + ', kw=' + kw + '] --- ratings.txt', delimiter='rate=',
                              usecols=1, dtype=str)

            tau, p_value = stats.kendalltau(score, rate)
            if(p_value < 0.05):
                  print(k)
                  print("Tau:", tau.__abs__())
                  print("P_value:", p_value)