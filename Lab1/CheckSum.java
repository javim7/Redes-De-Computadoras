/**
 * CheckSum.java: Implementación del algoritmo de Checksum
 * 
 * @version 1.0.0
 * @autor Javier Valle
 * @author Javier Mombiela
 * @date 01-08-2023
 * 
 */


 /**
  * CheckSumSender: Clase que implementa el algoritmo de Checksum para el envio de mensajes
  */
class CheckSumSender {

    private int blockSize = 8;
    public String message = "";
    public String dividedMessage = "";
    public String checkSum = "";
    public String messageWithCheckSum = "";


    /**
     * Constructor de la clase CheckSumSender
     * @param message Mensaje a enviar
     */
    public CheckSumSender(String message) {
        this.message = message;
    }

    /**
     * Divide el mensaje en bloques de 8 bits y agrega padding si es necesario
     * @return El mensaje dividido en bloques de 8 bits
     */
    public String divideMessage() {
        int messageLength = message.length();
        int paddingNeeded = blockSize - (messageLength % blockSize);
        
        // Only add padding if needed
        if (paddingNeeded < blockSize) {
            StringBuilder paddedMessage = new StringBuilder(message);
            for (int i = 0; i < paddingNeeded; i++) {
                paddedMessage.append("0");
            }
            message = paddedMessage.toString();
        }

        // Divide the message into blocks of blockSize
        StringBuilder dividedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); i += blockSize) {
            int endIndex = Math.min(i + blockSize, message.length());
            String block = message.substring(i, endIndex);
            dividedMessage.append(block).append(" ");
        }

        return dividedMessage.toString().trim();
    }

    /**
     * Calcula el CheckSum del mensaje
     * @return El CheckSum del mensaje
     */
    public String calculateCheckSum() {
        String[] blocks = this.dividedMessage.split(" ");

        // inicializa la suma en 0
        StringBuilder sumBinary = new StringBuilder("00000000");

        for (String block : blocks) {
            int blockValue = Integer.parseInt(block, 2); // Convert the block to an integer
            int sumValue = Integer.parseInt(sumBinary.toString(), 2); // Convert the sum to an integer

            int tempSum = blockValue + sumValue;

            // revisa si hay bits flotantes y los agrega al bit menos significativo
            if (tempSum > 255) {
                tempSum = (tempSum & 255) + 1; 
            }

            // Convierte la nueva suma a binario y agrega padding si es necesario
            String newSumBinary = Integer.toBinaryString(tempSum);
            while (newSumBinary.length() < 8) {
                newSumBinary = "0" + newSumBinary;
            }

            sumBinary = new StringBuilder(newSumBinary);
        }

        // System.out.println("Sum Binary: " + sumBinary);
        // Toma el complemento a 1 de la suma final
        for (int i = 0; i < sumBinary.length(); i++) {
            char bit = sumBinary.charAt(i);
            sumBinary.setCharAt(i, bit == '0' ? '1' : '0');
        }

        return sumBinary.toString();
    }

    /**
     * Imprime el mensaje, el mensaje dividido, el CheckSum y el mensaje final
     */
    public void sendMessage() {
        this.dividedMessage = this.divideMessage();
        System.out.println("Mensaje          -> " + this.message);
        System.out.println("Mensaje Dividido -> " + this.dividedMessage);
        this.checkSum = this.calculateCheckSum();
        System.out.println("CheckSum         -> " + this.checkSum);
        this.messageWithCheckSum = this.dividedMessage + " " + this.checkSum;
        System.out.println("Mensaje final    -> " + this.messageWithCheckSum);
    }
}


/**
 * CheckSumReceiver: Clase que implementa el algoritmo de Checksum para la recepción de mensajes
 */
class CheckSumReceiver {

    public String messageReceived = "";
    public String chekedSum = "";
    public CheckSumSender sender;


    /**
     * Constructor de la clase CheckSumReceiver
     * @param message Mensaje recibido
     */
    public CheckSumReceiver(String message) {
        this.messageReceived = message;
    }

    /**
     * Calcula el CheckSum del mensaje recibido
     * @return El CheckSum del mensaje recibido
     */
    public String checkTheSum() {
       String[] blocks = this.messageReceived.split(" ");

        // Inicializa la suma a 0
        StringBuilder sumBinary = new StringBuilder("00000000");

        for (String block : blocks) {
            int blockValue = Integer.parseInt(block, 2); // Convert the block to an integer
            int sumValue = Integer.parseInt(sumBinary.toString(), 2); // Convert the sum to an integer

            int tempSum = blockValue + sumValue;

            // Revisa si hay bits flotantes y los agrega al bit menos significativo
            if (tempSum > 255) {
                tempSum = (tempSum & 255) + 1; 
            }

            // Convierte la nueva suma a binario y agrega padding si es necesario
            String newSumBinary = Integer.toBinaryString(tempSum);
            while (newSumBinary.length() < 8) {
                newSumBinary = "0" + newSumBinary;
            }

            sumBinary = new StringBuilder(newSumBinary);
        }

        // System.out.println("Sum Binary: " + sumBinary);
        // Toma el complemento a 1 de la suma final
        for (int i = 0; i < sumBinary.length(); i++) {
            char bit = sumBinary.charAt(i);
            sumBinary.setCharAt(i, bit == '0' ? '1' : '0');
        }

        return sumBinary.toString();
    }

    /**
     * Analiza el CheckSum del mensaje recibido
     */
    public void analyzeSum() {
        if (this.chekedSum.equals("00000000")) {
            System.out.println("Resultado         -> La trama no contiene errores!");
        } else {
            System.out.println("Resultado         -> La trama contiene errores y sera descartada!");
        }
    }

    /**
     * Imprime el mensaje recibido, el CheckSum recibido y el resultado del análisis
     */
    public void receiveMessage() {
        System.out.println("Mensaje recibido  -> " + this.messageReceived);
        this.chekedSum = this.checkTheSum();
        System.out.println("CheckSum recibido -> " + this.chekedSum);
        this.analyzeSum();
    }
}


/**
 * CheckSum: Clase principal que ejecuta el programa
 */
class CheckSum {
    public static void main(String[] args) {
        System.out.println("\n----ENVIANDO MENSAJE----");
        CheckSumSender sender = new CheckSumSender("10101010");
        // CheckSumSender sender = new CheckSumSender("1010100100111001");
        // CheckSumSender sender = new CheckSumSender("10000100001001001110001010011001");
        sender.sendMessage();
        String checksum = sender.checkSum;

        System.out.println("\n----RECIBIENDO MENSAJE----");
        CheckSumReceiver receiver = new CheckSumReceiver("10101011 " + checksum);
        receiver.receiveMessage();
    }
}