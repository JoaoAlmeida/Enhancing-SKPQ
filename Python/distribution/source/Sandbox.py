arr = [-4, 3, -9, 0, 4, 1]

count = [0,0,0]

for x in range(0, len(arr)):
    if arr[x] > 0:
        count[0] = count[0] + 1
    elif 0 == arr[x]:
        count[2] = count[2] + 1
    else:
        count[1] = count[1] + 1
for y in range(0,len(count)):
    count[y] = count[y] / len(arr)
