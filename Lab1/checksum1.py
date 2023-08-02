def fletcher_checksum(data, block_size):
    # Tamaño de la palabra binaria (8 bits)
    word_size = 8

    # Calcular la cantidad de bloques necesarios para cubrir la longitud k
    num_blocks = (len(data) + block_size - 1) // block_size

    # Agregar ceros de relleno si es necesario para completar el último bloque
    padded_data = data.ljust(num_blocks * block_size, '0')

    # Inicialización de las variables sum1 y sum2
    sum1 = 0
    sum2 = 0

    # Recorrer los datos binarios y calcular las sumas
    for i in range(0, len(padded_data), block_size):
        block = padded_data[i:i+block_size]

        # Convertir el bloque a un número entero
        num = int(block, 2)

        # Agregar el número actual a sum1
        sum1 = (sum1 + num) % 255

        # Agregar el valor actual de sum1 a sum2
        sum2 = (sum2 + sum1) % 255

    # Calcular el checksum final concatenando sum1 y sum2
    checksum = (sum2 << word_size) | sum1

    return checksum


# Pasándole este binario 10011110011 (número binario representado como una cadena de caracteres).
binary_data = '10011110011'
block_size = 8
original_checksum = fletcher_checksum(binary_data, block_size)

# Imprimir el resultado del cálculo original
print("El checksum original es:", original_checksum)

# Supongamos que se transmiten o almacenan los datos binarios junto con el checksum original.
# Ahora, para verificar si el binario está correcto, volvemos a calcular el checksum y lo comparamos con el original.

# Datos binarios recibidos (pueden contener errores)
received_binary_data = '10011110011'
received_checksum = fletcher_checksum(received_binary_data, block_size)

# Imprimir el resultado del cálculo recibido
print("El checksum recibido es:", received_checksum)

# Verificar si los checksums coinciden o no
if original_checksum == received_checksum:
    print("Los datos binarios están correctos.")
else:
    print("Los datos binarios están incorrectos. Puede haber errores.")
