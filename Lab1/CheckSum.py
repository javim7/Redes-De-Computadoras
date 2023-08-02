"""
CheckSum.py: Implementacion del algoritmo de Checksum para la deteccion de errores en tramas de datos.

Autores: Javier Valle, Javier Mombiela
Fecha: 01/08/2023
"""


"""
CheckSumSender: Clase que implementa el algoritmo de Checksum para la deteccion de errores en tramas de datos.
"""
class CheckSumSender:

    def __init__(self, message):
        """
        Constructor de la clase CheckSumSender.

        :param message: Trama de datos a enviar.
        """

        self.blockSize = 8
        self.message = message
        self.dividedMessage = ""
        self.checkSum = ""
        self.messageWithCheckSum = ""

    def divideMessage(self):
        """
        metodo para dividir la trama de datos en bloques de 8 bits.

        :return: Trama de datos dividida en bloques de 8 bits.
        """

        messageLength = len(self.message)
        paddingNeeded = self.blockSize - (messageLength % self.blockSize)

        # Only add padding if needed
        if paddingNeeded < self.blockSize:
            paddedMessage = self.message + "0" * paddingNeeded
            self.message = paddedMessage

        # Divide the message into blocks of blockSize
        dividedMessage = " ".join([self.message[i:i+self.blockSize] for i in range(0, len(self.message), self.blockSize)])
        self.dividedMessage = dividedMessage.strip()
        return self.dividedMessage

    def calculateCheckSum(self):
        """
        Metodo para calcular el Checksum de la trama de datos.

        :return: Checksum de la trama de datos.
        """

        blocks = self.dividedMessage.split(" ")

        # initialize the sum to 0
        sumBinary = "00000000"

        for block in blocks:
            blockValue = int(block, 2)  # Convert the block to an integer
            sumValue = int(sumBinary, 2)  # Convert the sum to an integer

            tempSum = blockValue + sumValue

            # check for carry and add it to the least significant bit
            if tempSum > 255:
                tempSum = (tempSum & 255) + 1

            # Convert the new sum to binary and add padding if necessary
            newSumBinary = bin(tempSum)[2:].zfill(8)

            sumBinary = newSumBinary

        # Take the one's complement of the final sum
        sumBinary = "".join('1' if bit == '0' else '0' for bit in sumBinary)

        self.checkSum = sumBinary
        return self.checkSum

    def sendMessage(self):
        """
        Metodo para enviar la trama de datos con el Checksum.
        """

        self.dividedMessage = self.divideMessage()
        print("Mensaje          ->", self.message)
        print("Mensaje Dividido ->", self.dividedMessage)
        self.checkSum = self.calculateCheckSum()
        print("CheckSum         ->", self.checkSum)
        self.messageWithCheckSum = self.dividedMessage + " " + self.checkSum
        print("Mensaje final    ->", self.messageWithCheckSum)

"""
CheckSumReceiver: Clase que implementa el algoritmo de Checksum para la deteccion de errores en tramas de datos.
"""
class CheckSumReceiver:

    def __init__(self, message):
        """
        Constructor de la clase CheckSumReceiver.

        :param message: Trama de datos a recibir.
        """

        self.messageReceived = message
        self.chekedSum = ""
        self.sender = None

    def checkTheSum(self):
        """
        Metodo para calcular el Checksum de la trama de datos recibida.

        :return: Checksum de la trama de datos recibida.
        """

        blocks = self.messageReceived.split(" ")

        # initialize the sum to 0
        sumBinary = "00000000"

        for block in blocks:
            blockValue = int(block, 2)  # Convert the block to an integer
            sumValue = int(sumBinary, 2)  # Convert the sum to an integer

            tempSum = blockValue + sumValue

            # check for carry and add it to the least significant bit
            if tempSum > 255:
                tempSum = (tempSum & 255) + 1

            # Convert the new sum to binary and add padding if necessary
            newSumBinary = bin(tempSum)[2:].zfill(8)

            sumBinary = newSumBinary

        # Take the one's complement of the final sum
        sumBinary = "".join('1' if bit == '0' else '0' for bit in sumBinary)

        self.chekedSum = sumBinary
        return self.chekedSum

    def analyzeSum(self):
        """
        Metodo para analizar el Checksum de la trama de datos recibida.
        """

        if self.chekedSum == "00000000":
            print("Resultado         -> La trama no contiene errores!")
        else:
            print("Resultado         -> La trama contiene errores y será descartada!")

    def receiveMessage(self):
        """
        Metodo para recibir la trama de datos con el Checksum.
        """

        print("Mensaje recibido  ->", self.messageReceived)
        self.chekedSum = self.checkTheSum()
        print("CheckSum recibido ->", self.chekedSum)
        self.analyzeSum()

"""
CheckSum: Clase que implementa el algoritmo de Checksum para la deteccion de errores en tramas de datos.
"""
class CheckSum:

    def __init__(self):
        """
        Constructor de la clase CheckSum.
        """

        print("----ENVIANDO MENSAJE----")
        message = "1010100100111001"
        sender = CheckSumSender(message)
        sender.sendMessage()
        checksum = sender.checkSum

        print("\n----RECIBIENDO MENSAJE----")
        message2 = "10101001 00111001"
        receiver = CheckSumReceiver(message2 + " " + checksum)
        receiver.receiveMessage()

if __name__ == '__main__':
    CheckSum()
