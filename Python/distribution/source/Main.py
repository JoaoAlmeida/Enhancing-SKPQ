import matplotlib.pyplot as plt
import numpy
import re
#a, m = 3., 2.  # shape and mode
#s = (numpy.random.pareto(a, 1000) + 1) * m
#print(s)
#count, bins, _ = plt.hist(s, 100, density=True)
#fit = a*m**a / bins**(a+1)
#plt.plot(bins, max(count)*fit/max(fit), linewidth=2, color='r')
#plt.show()

f=open("user profile 4162-Agruped-Dist.txt", "r")

lines = f.readlines()
dist = []
#for x in lines:
s = lines[0].splitlines()
vec=s[0].split(",")
for y in vec:
    dist.append(float(y))
print(dist)

plt.hist(dist, bins=10)
plt.show()

a, m = 3., 2.  # shape and mode
s = (numpy.random.pareto(a, 1000) + 1) * m

count, bins, _ = plt.hist(s, 100, density=True)
fit = a*m**a / bins**(a+1)
plt.plot(bins, max(count)*fit/max(fit), linewidth=2, color='r')
plt.show()