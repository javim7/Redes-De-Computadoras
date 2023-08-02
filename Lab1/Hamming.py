class Hamming():
    def __init__(self):
        pass

    def binaryToDecimal(self, binary):
        #1011
        decimal = 0
        binary = binary[::-1]
        for i in range(len(binary)):
            if binary[i]=="0":
                continue
            else:
                decimal += 2**i
        return decimal
    
hamming = Hamming()

print(hamming.binaryToDecimal("1011"))