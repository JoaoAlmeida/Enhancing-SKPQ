import numpy as np
import matplotlib.pyplot as plt

#Ux -> Unicode String <número max de caracteres>
#Sx -> String <número de caracteres>
#Pode usar também np.<tipo>. O autocomplete ajuda.

data = np.loadtxt('./files/object of interest/alcohol.txt', delimiter='\t', usecols=(0,3), dtype={'names': ('id', 'category'), 'formats': (np.int, 'U15')})
