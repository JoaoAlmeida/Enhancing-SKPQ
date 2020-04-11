import statsmodels.api as sm
import numpy as np

data = np.loadtxt('distances.txt', dtype={'names': ('distance', 'probability'), 'formats': ('f8', 'f8')})

t = sm.Probit(data['distance'],).fit()

#print(result.summary())