"""
Hamming.py: Implementacion del algoritmo de Hamming para la deteccion y correccion de errores en tramas de datos.

Autores: Javier Valle, Javier Mombiela
Fecha: 01/08/2023
"""

import math

"""
HammingCoding: Clase que implementa el algoritmo de Hamming para la codificacion de tramas de datos.
"""
class HammingCoding:
    def __init__(self, n, m, dataSent):
        """
        Constructor de la clase HammingCoding.

        :param n: Numero total de bits de la trama codificada.
        :param m: Numero de bits de la trama original.
        :param dataSent: Trama de datos a codificar.
        """
        r = n - m
        if (m + r + 1) <= 2 ** r:
            self.data = dataSent
        else:
            raise ValueError("\nCodigo invalido, no cumple con la siguiente condicion: (m + r + 1) <= 2^r")

    def calculateParityBits(self):
        """
        Metodo para calcular el numero de bits de paridad necesarios para la codificacion de la trama.
        
        :return: Numero de bits de paridad necesarios para la codificacion de la trama.
        """
        m = len(self.data)
        r = 0
        while 2 ** r < m + r + 1:
            r += 1
        self.totalBits = m + r
        self.parityBitCount = r
        return r

    def calculateParity(self, parityIndex, hammingCode):
        """
        Metodo para calcular el valor del bit de paridad en la posicion dada.

        :param parityIndex: Posicion del bit de paridad.
        :param hammingCode: Arreglo con la trama codificada.
        :return: Valor del bit de paridad en la posicion dada.
        """

        parityValue = 0
        for i in range(1, self.totalBits + 1):
            if i & parityIndex != 0:
                if hammingCode[i].startswith("P"):
                    continue
                bit = int(hammingCode[i])
                parityValue ^= bit
        return parityValue

    def definePositions(self):
        """
        Metodo para definir las posiciones de los bits de paridad y los bits de datos en la trama codificada.

        :return: Arreglo con la trama codificada.
        """

        hammingCodeArray = [None] * (self.totalBits + 1)
        j = 0
        for i in range(1, self.totalBits + 1):
            if i == 2 ** j:
                hammingCodeArray[i] = "P" + str(j + 1)
                j += 1
            else:
                hammingCodeArray[i] = self.data[i - j - 1]

        for j in range(self.parityBitCount):
            parityIndex = 2 ** j
            parityValue = self.calculateParity(parityIndex, hammingCodeArray)
            hammingCodeArray[parityIndex] = "(" + str(parityValue) + ")"

        return hammingCodeArray

    def getCode(self, hammingCodeArray):
        """
        Metodo para obtener la trama codificada y los bits de paridad.

        :param hammingCodeArray: Arreglo con la trama codificada.
        :return: Trama codificada y bits de paridad.
        """

        hammingCode = ""
        bitsInsideParentheses = ""
        for i in range(1, len(hammingCodeArray)):
            if hammingCodeArray[i] is not None:
                hammingCode += hammingCodeArray[i]
                if hammingCodeArray[i].startswith("(") and hammingCodeArray[i].endswith(")"):
                    bitsInsideParentheses += hammingCodeArray[i][1:-1]

        return hammingCode.replace("(", "").replace(")", ""), bitsInsideParentheses

    def fullCoding(self):
        """
        Metodo para realizar la codificacion de la trama de datos.
        """

        # print("Trama ingresada           ->", self.data)
        self.calculateParityBits()
        # print("Numero de bits de paridad ->", self.calculateParityBits())
        hammingCodeArray = self.definePositions()
        self.hammingCode, self.parityBits = self.getCode(hammingCodeArray)
        # print("Bits de paridad           ->", self.parityBits)
        # print("Codigo                    ->", self.hammingCode)


"""
HammingDecoding: Clase que implementa el algoritmo de Hamming para la decodificacion de tramas de datos.
"""
class HammingDecoding:
    def __init__(self, data, wrongData):
        """
        Constructor de la clase HammingDecoding.

        :param data: Trama de datos original.
        :param wrongData: Trama de datos con errores.
        """

        self.data = data
        self.wrongData = wrongData
        # print("Trama recibida            ->", self.data)
        # print("Trama con errores         ->", self.wrongData)

    def removeParityBits(self):
        """
        Metodo para eliminar los bits de paridad de la trama de datos con errores.

        :return: Trama de datos sin bits de paridad.
        """

        dataWithoutParityBits = ""
        self.parityBits = ""
        for i in range(len(self.wrongData)):
            if (i + 1) != 2 ** int(math.floor(math.log(i + 1) / math.log(2))):
                dataWithoutParityBits += self.wrongData[i]
            else:
                self.parityBits += self.wrongData[i]
        return dataWithoutParityBits

    def compareParityBits(self, parity1, parity2):
        """
        Metodo para comparar los bits de paridad de la trama original y la trama con errores.

        :param parity1: Bits de paridad de la trama original.
        :param parity2: Bits de paridad de la trama con errores.
        :return: Bits de error.
        """

        errorBits = ""
        for i in range(len(parity1)):
            if parity1[i] != parity2[i]:
                errorBits += "1"
            else:
                errorBits += "0"
        reversedErrorBits = errorBits[::-1]  
        return reversedErrorBits

    def binaryToDecimals(self):
        """
        Metodo para convertir los bits de error a decimal.

        :return: Bits de error en decimal.
        """

        decimal = 0
        reversedErrorBits = self.errorBits[::-1]
        for i in range(len(reversedErrorBits)):
            if reversedErrorBits[i] == '1':
                decimal += 2 ** i
        return decimal

    def correctErrors(self, originalParity):
        """
        Metodo para corregir los errores en la trama de datos.

        :param originalParity: Bits de paridad de la trama original.
        :return: Trama de datos corregida.
        """

        errorCount = 0
        parityCompared = self.compareParityBits(originalParity, self.parityBits)[::-1]

        correct = ""
        errorBitIndex = -1

        for i in range(len(self.errorBits)):
            if self.errorBits[i] == '1':
                errorCount += 1

        for i in range(len(parityCompared)):
            if parityCompared[i] == '1':
                errorCount += 1
                errorBitIndex = i

        if errorCount == 0:
            # print("\nNo se detectaron errores.")
            correct = self.wrongData
        elif errorCount == 1:
            wrongPosition = 2 ** errorBitIndex
            bitValue = self.wrongData[wrongPosition - 1]
            print(wrongPosition)
            correct = self.wrongData[:wrongPosition - 1] + ('1' if bitValue == '0' else '0') + self.wrongData[wrongPosition:]
            # print("\nSe detectó un error, se corrigió el bit de paridad en la posición", wrongPosition)
        else:
            dataBitPosition = self.binaryToDecimals()
            bitValue = self.wrongData[dataBitPosition - 1]
            print(dataBitPosition)
            correct = self.wrongData[:dataBitPosition - 1] + ('1' if bitValue == '0' else '0') + self.wrongData[dataBitPosition:]
            # print("\nSe detectaron varios errores de paridad, se corrigió el bit en la posición", dataBitPosition)

        return correct

    def fullDecoding(self, hamming):
        """
        Metodo para realizar la decodificacion de la trama de datos con errores.
        """

        self.dataWithoutParityBits = self.removeParityBits()
        haming2 = HammingCoding(11, 7, self.dataWithoutParityBits)
        haming2.fullCoding()
        self.errorBits = self.compareParityBits(hamming.parityBits, haming2.parityBits)
        self.correctData = self.correctErrors(hamming.parityBits)
        # print("Trama original  ->", self.data)
        # print("Trama corregida ->", self.correctData)


"""
Main: Clase principal del programa.
"""
class Hamming:

    def __init__(self):
        """
        Constructor de la clase Main.
        """

        #codificacion
        try:
            hamming = HammingCoding(12, 8, "01100001")
            print("\n-------CODIFICACION--------")
            hamming.fullCoding()
        except ValueError as e:
            print(e)
            return

        #decodificacion
        try:
            print("\n--------DECODIFICACION--------")
            hammingDecoding = HammingDecoding(hamming.hammingCode, "111111010011")
            hammingDecoding.fullDecoding(hamming)
        except Exception as e:
            print("\nSe ha detectado mas de un error en la trama!")
            return


if __name__ == "__main__":
    Hamming()