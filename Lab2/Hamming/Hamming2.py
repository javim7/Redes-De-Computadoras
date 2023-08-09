import socket
import random
import math
from Hamming import *

class HammEmisor:

    def __init__(self):
        self.HOST = "127.0.0.1"
        self.PORT = 5555
        self.text = ""
        self.coded_message = ""
        self.message_with_integrity = ""
        self.message_with_noise = ""

    def request_text(self):
        text = input("\nEnter the text to send: ")
        return text

    def code_message(self, text):
        binary = ""

        for char in text:
            ascii_value = ord(char)
            binary_string = bin(ascii_value)[2:].zfill(8)
            binary += binary_string

        return binary

    def calculate_parity_bits(self, text):
        m = len(text)
        r = 0

        while 2 ** r < m + r + 1:
            r += 1

        return r

    def calculate_integrity(self, text):
        n = len(text) + self.calculate_parity_bits(text)
        hamming = HammingCoding(n, len(text), text)
        hamming.fullCoding()
        return hamming.hammingCode

    def calculate_noise_probability(self, text):
        length = len(text)
        errors_in_interval = length / 2

        base_prob = 1 / errors_in_interval
        adjustment = base_prob / 2

        random_value = random.random()
        if random_value < 0.5:
            return base_prob - adjustment
        else:
            return base_prob + adjustment

    def add_noise(self, text):
        noise_probability = self.calculate_noise_probability(text)
        noisy_text = ""

        for bit in text:
            random_value = random.random()

            if random_value < noise_probability:
                noisy_bit = '1' if bit == '0' else '0'
            else:
                noisy_bit = bit

            noisy_text += noisy_bit

        return noisy_text

    def send_data(self, original_data, noisy_data):
        print("\nStarting sender")
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as socket_client:
                socket_client.connect((self.HOST, self.PORT))
                payload = f"{original_data},{noisy_data}"
                print("Sending data")
                socket_client.sendall(payload.encode())
                socket_client.shutdown(socket.SHUT_WR)
                print("Socket released")
        except Exception as e:
            print(e)

    def full_emisor(self):
        self.text = self.request_text()
        self.coded_message = self.code_message(self.text)
        print("Coded message              :", self.coded_message)
        self.message_with_integrity = self.calculate_integrity(self.coded_message)
        self.message_with_noise = self.add_noise(self.message_with_integrity)
        print("Message with noise         :", self.message_with_noise)

        try:
            self.send_data(self.coded_message, self.message_with_noise)
        except Exception as e:
            print(e)

class HammReceptor():
    def __init__(self):
        self.HOST = "127.0.0.1"
        self.PORT = 5555
        self.errors = False
        self.ogData = ""
        self.noisyData = ""
        self.originalData = ""
        self.decodedMessage = ""

    def receive(self):
        data = ""
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            #asignar socket a ip: puerto especifico
            s.bind((self.HOST, self.PORT))
            s.listen()

            # esperar conexion
            conn, addr = s.accept()
            with conn:
                print(f"\nConexion entrante del proceso {addr}")
                while True:
                    data_received = conn.recv(1024)
                    if not data_received:
                        break # se termino de recibir todo
                    data += data_received.decode("utf-8")
                    print(f"\nRecibido: \n{data!r}")
        
        return data
    
    def calculateParityBits(self, data):
        m = len(data)
        r = 0
        while 2 ** r < m + r + 1:
            r += 1
        
        return r

    def compareData(self, ogData, noisyData):
        if ogData == noisyData:
            self.errors = False
            print("\nIntegridad verficada y trama correcta!")
        else:
            self.errors = True
            print("\nIntegridad Incorrecta!")
    
    def eliminateParityBits(self, data):
        original_data = ""

        # Iterate through the data and skip bits at positions that are powers of 2
        for i, bit in enumerate(data, start=1):
            if i & (i - 1) == 0:
                continue  # Skip bits at powers of 2
            original_data += bit

        return original_data

    def deCodeMessage(self, data):
        if self.errors:
            return "\nError: No se puede decodificar el mensaje!"
        else:
            # Split the data into 8-bit chunks
            binary_chunks = [data[i:i+8] for i in range(0, len(data), 8)]

            # Convert each binary chunk to ASCII value
            ascii_values = [int(chunk, 2) for chunk in binary_chunks]

            # Convert ASCII values to characters and join to form the original text
            original_text = ''.join(chr(value) for value in ascii_values)
            
            return original_text

    def fullReceptor(self):
        print("\nEsperando mensaje...")
        dataRecieved = self.receive()
        dataRecieved = dataRecieved.split(",")
        self.ogData = dataRecieved[0]
        self.noisyData = dataRecieved[1]
        print("\nTrama original            ->", self.ogData)
        print("Trama con errores         ->", self.noisyData)

        #codificando y decodificando con los algoritmos originales
        n = self.calculateParityBits(self.ogData) + len(self.ogData)
        hamming = HammingCoding(n, len(self.ogData), self.ogData)
        hamming.fullCoding()
        try:
            hammingDecoding = HammingDecoding(hamming.hammingCode, self.noisyData)
            hammingDecoding.fullDecoding(hamming)
            self.compareData(hamming.hammingCode, hammingDecoding.correctData)
            self.originalData = self.eliminateParityBits(hammingDecoding.correctData)
            if self.errors == False:
                print("Trama corregida      ->", self.originalData)
                self.decodedMessage = self.deCodeMessage(self.originalData)
                print("Mensaje decodificado ->", self.decodedMessage)
            else:
                print(self.decodedMessage(self.noisyData))
        except Exception as e:
            self.errors = True
            self.decodedMessage = self.deCodeMessage(self.noisyData)
            print(self.decodedMessage)

class Hamming2():
    def __init__(self):
        print("\nQue desea hacer?")
        print("1. Emisor")
        print("2. Receptor")

        opcion = int(input("Opcion -> "))

        if opcion == 1:
            self.emisor()
        elif opcion == 2:
            self.receptor()

    def emisor(self):
        emisor = HammEmisor()
        emisor.full_emisor()

    def receptor(self):
        receptor = HammReceptor()
        receptor.fullReceptor()

if __name__ == "__main__":
    Hamming2()