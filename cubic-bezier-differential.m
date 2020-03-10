
p4 = (p1 - p0) * t + p0
p5 = (p2 - p1) * t + p1
p6 = (p3 - p2) * t + p2

p7 = (p5 - p4) * t + p4
p8 = (p6 - p5) * t + p5

p9 = (p8 - p7) * t + p7

Print[Simplify[D[p9, t]]]
