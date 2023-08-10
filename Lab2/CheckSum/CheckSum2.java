package CheckSum;

import java.util.ArrayList;
import java.util.Scanner;

import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

class CheckEmisor {
    
    private static String HOST = "127.0.0.1";
    private static int PORT = 5555;
    public String text = "";
    public String codedMessage = "";
    public String messageWithIntegrity = "";
    public String messageWithNoise = "";

    public CheckEmisor(String text) {
        this.text = text;
    }

    public String requestText() {
        System.out.println("\nIngrese el texto a enviar: ");
        Scanner scanner = new Scanner(System.in);
        String text = scanner.nextLine();
        scanner.close();
        return text;
    }

    public String codeMessage(String text) {
        StringBuilder binary = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int asciiValue = (int) character;
            
            String binaryString = Integer.toBinaryString(asciiValue);
            
            while (binaryString.length() < 8) {
                binaryString = "0" + binaryString;
            }
            
            binary.append(binaryString);
        }
        
        return binary.toString();
    }

    public String calculateIntegrity(String text) {
        CheckSumSender checkSum = new CheckSumSender(text);
        checkSum.sendMessage();
        String messageWithIntegrity = checkSum.messageWithCheckSum;
        return messageWithIntegrity;
    }

    public double calculateNoiseProbability(String text) {
        int length = text.length();
        double errorsInInterval = length;

        double baseProb = 1 / errorsInInterval;
        double adjustment = baseProb / 2;

        return baseProb;
    }

    public String addNoise(String text) {
        double noiseProbability = calculateNoiseProbability(text);
        // System.out.println("Probabilidad de ruido: " + noiseProbability);
        // noiseProbability = 0.08;
        StringBuilder noisyText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char originalBit = text.charAt(i);
            char noisyBit;

            double randomValue = Math.random();

            //verificar si se voltean los bits
            if (randomValue < noiseProbability) {
                noisyBit = (originalBit == '0') ? '1' : '0'; 
            } else {
                noisyBit = originalBit; 
            }

            noisyText.append(noisyBit);
        }
        // System.out.println("Noisy: " + noisyText.toString());
        return noisyText.toString();
    }

    public void sendData(Socket socketClient, String payload)
        throws IOException, UnknownHostException, InterruptedException {

            OutputStreamWriter writer = null;
            // System.out.println("\nIniciando emisor");

            payload += "\n";
            //mandar data
            // System.out.println("Enviando data");
            writer = new OutputStreamWriter(socketClient.getOutputStream());
            writer.write(payload);
            writer.flush();
            Thread.sleep(100); // espera, opcional

            //limpieza
            // System.out.println("Liberando socket");
            // writer.close();

        }

    public void fullEmisor(Socket socketClient) {
        // this.text = requestText();
        this.codedMessage = codeMessage(this.text);
        this.messageWithIntegrity = calculateIntegrity(this.codedMessage);
        this.messageWithNoise = addNoise(this.messageWithIntegrity);

        // System.out.println("\nTexto original      : " + this.text);
        // System.out.println("Mensaje codificado  : " + this.codedMessage);
        // System.out.println("Mensaje con CheckSum: " + this.messageWithIntegrity);
        System.out.println("Mensaje con ruido   : " + this.messageWithNoise);

        try {
            sendData(socketClient, this.messageWithNoise);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}

class CheckReceptor {

    private int PORT = 5555;
    private boolean errors = false;
    private String dataReceived = "";
    private String decodedMessage = "";

    public String receive() throws IOException {
        String data = "";
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("\nEsperando mensaje...");
            Socket socket = serverSocket.accept();
            // System.out.println("Conexion aceptada de: " + socket.getInetAddress());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                data += new String(buffer, 0, bytesRead);
            }
        }
        return data;
    }

    public String verityIntegrity(String data) {
        CheckSumReceiver receiver = new CheckSumReceiver(data);
        receiver.receiveMessage();

        if (receiver.chekedSum.equals("00000000")) {
            errors = false;
            System.out.println("\nIntegridad verificada y trama correcta!");
        } else {
            errors = true;
            System.out.println("\nIntegridad Incorrecta!");
        }

        return receiver.chekedSum;
    }

    public String decodeMessage(String data) {
        if (errors) {
            return "\nLa trama contiene errores y será descartada!";
        } else {
            data = data.substring(0, data.length() - 9);
            String[] chunks = data.split(" ");
            StringBuilder originalText = new StringBuilder();

            for (String chunk : chunks) {
                int asciiValue = Integer.parseInt(chunk, 2);
                originalText.append((char) asciiValue);
            }

            return originalText.toString();
        }
    }

    public void fullReceptor() {
        try {
            this.dataReceived = receive();
            verityIntegrity(dataReceived);
            this.decodedMessage = decodeMessage(dataReceived);

            System.out.println("\nMensaje decodificado: " + decodedMessage);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

public class CheckSum2 {
    public static void main(String[] args) {
        String HOST = "127.0.0.1";
        int PORT = 5555;
        // Scanner scanner = new Scanner(System.in);
        // System.out.println("\n----CHECKSUM----");
        // System.out.println("Que desea hacer?");
        // System.out.println("1. Emisor");
        // System.out.println("2. Receptor");
        // System.out.print("Opcion -> ");
        // int option = scanner.nextInt();

        int option = 1;
        
       if (option == 1) {
            ArrayList<String> text = new ArrayList<>();

            // Read from the tests.txt file and populate the 'text' ArrayList
            try (BufferedReader reader = new BufferedReader(new FileReader("tests.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    text.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return; // Exit the program if there's an error reading the file
            }

            try {
                // Establish socket connection outside the loop
                Socket socketClient = new Socket(InetAddress.getByName(HOST), PORT);

                for (String t : text) {
                    CheckEmisor sender = new CheckEmisor(t);
                    sender.fullEmisor(socketClient);
                }

                socketClient.close(); // Close the socket connection after sending all messages
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Mensajes enviados.");
        } else if (option == 2) {
            
            CheckReceptor receiver = new CheckReceptor();
            receiver.fullReceptor();
            
        } else {
            System.out.println("Opcion Invalida.");
        }

        // scanner.close();
    }
}
