import socket
import random
from CheckSum import *

class CheckEmisor:

    def __init__(self):
        self.HOST = "127.0.0.1"
        self.PORT = 5555
        self.text = ""
        self.codedMessage = ""
        self.messageWithIntegrity = ""
        self.messageWithNoise = ""

    def request_text(self):
        print("\nIngrese el texto a enviar: ")
        text = input()
        return text

    def code_message(self, text):
        binary = ""
        for character in text:
            ascii_value = ord(character)
            binary_string = bin(ascii_value)[2:].zfill(8)
            binary += binary_string
        return binary

    def calculate_integrity(self, text):
        checkSum = CheckSumSender(text)
        checkSum.sendMessage()
        message_with_integrity = checkSum.messageWithCheckSum
        return message_with_integrity

    def calculate_noise_probability(self, text):
        length = len(text)
        errors_in_interval = length

        base_prob = 1 / errors_in_interval
        adjustment = base_prob / 2

        random_value = random.random()
        if random_value < 0.33:
            return base_prob - adjustment
        elif 0.33 <= random_value < 0.66:
            return base_prob + adjustment
        else:
            return base_prob

    def add_noise(self, text):
        noise_probability = self.calculate_noise_probability(text)
        # print("Probabilidad de ruido:", noise_probability)
        # noise_probability = 0
        noisy_text = ""

        for original_bit in text:
            random_value = random.random()

            if random_value < noise_probability:
                noisy_bit = '1' if original_bit == '0' else '0'
            else:
                noisy_bit = original_bit

            noisy_text += noisy_bit
        return noisy_text

    def send_data(self, payload):
        print("\nIniciando emisor")
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as socket_client:
                socket_client.connect((self.HOST, self.PORT))
                print("Enviando data")
                socket_client.sendall(payload.encode())
                socket_client.close()
                print("Liberando socket")
        except Exception as e:
            print("Error:", e)

    def full_emisor(self):
        self.text = self.request_text()
        self.codedMessage = self.code_message(self.text)
        self.messageWithIntegrity = self.calculate_integrity(self.codedMessage)
        self.messageWithNoise = self.add_noise(self.messageWithIntegrity)

        print("\nTexto original      :", self.text)
        print("Mensaje codificado  :", self.codedMessage)
        print("Mensaje con CheckSum:", self.messageWithIntegrity)
        print("Mensaje con ruido   :", self.messageWithNoise)

        self.send_data(self.messageWithNoise)

class CheckReceptor():
    def __init__(self):
        self.HOST = "127.0.0.1"
        self.PORT = 5555
        self.errors = False
        self.dataRecieved = ""
        self.checkedSum = ""
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
    
    def verityIntegrity(self, data):
        receiver = CheckSumReceiver(data)
        receiver.receiveMessage()

        if receiver.chekedSum == "00000000":
            self.errors = False
            print("\nIntegridad verficada y trama correcta!")
        else:
            self.errors = True
            print("\nIntegridad Incorrecta!")

        return receiver.chekedSum
    
    def decodeMessage(self, data):
        if self.errors:
            return "\nError: La trama contiene errores y será descartada!"
        else:
            data = data[:-9]
            # Convert each binary chunk to ASCII value
            ascii_values = [int(chunk, 2) for chunk in data.split()]

            # Convert ASCII values to characters and join to form the original text
            original_text = ''.join(chr(value) for value in ascii_values)
            
            return original_text

    def fullReceptor(self):
        print("\nEsperando mensaje...")
        self.dataRecieved = self.receive()
        # print("\nMensaje recibido: ", self.dataRecieved)
        self.checkedSum = self.verityIntegrity(self.dataRecieved)
        # print("\nCheckSum recibido: ", self.checkedSum)
        self.decodedMessage = self.decodeMessage(self.dataRecieved)
        print("\nMensaje decodificado: ", self.decodedMessage)

        

class CheckSum2():
    def __init__(self):
        print("\n---CheckSum---")
        print("Que desea hacer?")
        print("1. Emisor")
        print("2. Receptor")

        opcion = int(input("Opcion -> "))

        if opcion == 1:
            self.emisor()
        elif opcion == 2:
            self.receptor()

    def emisor(self):
        emisor = CheckEmisor()
        emisor.full_emisor()

    def receptor(self):
        receptor = CheckReceptor()
        receptor.fullReceptor()

if __name__ == "__main__":
    CheckSum2()