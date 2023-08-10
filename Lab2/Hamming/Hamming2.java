package Hamming;

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

class HammEmisor {

    private static String HOST = "127.0.0.1";
    private static int PORT = 5555;
    public String text = "";
    public String codedMessage = "";
    public String messageWithIntegrity = "";
    public String messageWithNoise = "";

    public HammEmisor(String text) {
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

    public int calculateParityBits(String text) {
        int m = text.length();
        int r = 0;

        while (Math.pow(2, r) < m + r + 1) {
            r++;
        }

        return r;
    }

    public String calculateIntegrity(String text) {
        int n = text.length() + calculateParityBits(text);
        HammingCoding hamming = new HammingCoding(n, text.length(), text);
        hamming.fullCoding();
        return hamming.hammingCode;
    }

    public double calculateNoiseProbability(String text) {
        int length = text.length();
        double errorsInInterval = length/2;

        double baseProb = 1 / errorsInInterval; //0.25
        return baseProb;

        // double randomValue = Math.random();
        // if (randomValue < 0.33) {
        //     return baseProb - adjustment;
        // } else if (randomValue < 0.66 && randomValue >= 0.33) {
        //     return baseProb + adjustment;
        // } else {
        //     return baseProb;
        // }
    }

    public String addNoise(String text) {
        double noiseProbability = calculateNoiseProbability(text);
        System.out.println("Probabilidad de ruido: " + noiseProbability);
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

        return noisyText.toString();
    }

    private void sendData(Socket socketClient, String originalData, String noisyData)
        throws IOException, InterruptedException {

        OutputStreamWriter writer = null;

        //crear payload
        String payload = originalData + "," + noisyData+ "\n";

        //mandar data
        // System.out.println("Enviando data");
        writer = new OutputStreamWriter(socketClient.getOutputStream());
        writer.write(payload);
        writer.flush(); // Flush the output stream to ensure data is sent
        Thread.sleep(100); // espera, opcional

        //limpieza
        // System.out.println("Liberando socket");
        // writer.close();
    }
    
    public void fullEmisor(Socket socketClient) {
        // this.text = this.requestText();
        this.codedMessage = this.codeMessage(this.text);
        // System.out.println("Mensaje codificado         : " + this.codedMessage);
        this.messageWithIntegrity = this.calculateIntegrity(this.codedMessage);
        this.messageWithNoise = this.addNoise(this.messageWithIntegrity);
        System.out.println("Mensaje con ruido          : " + this.messageWithNoise);

        try {
            sendData(socketClient, this.codedMessage, this.messageWithNoise);
        } catch (Exception e) {
            e.printStackTrace();
        }
}
    
}

class HammReceptor {

    // private final String HOST = "127.0.0.1";
    private final int PORT = 5555;
    private boolean errors = false;
    private String ogData = "";
    private String noisyData = "";
    private String originalData = "";
    private String decodedMessage = "";

    public String receive() throws IOException {
        String data = "";
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("\nEsperando mensaje...");
            Socket socket = serverSocket.accept();
            System.out.println("Conexion aceptada de: " + socket.getInetAddress());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                data += new String(buffer, 0, bytesRead);
            }
        }
        return data;
    }

    public int calculateParityBits(String data) {
        int m = data.length();
        int r = 0;
        while (Math.pow(2, r) < m + r + 1) {
            r++;
        }
        return r;
    }

    public void compareData(String ogData, String noisyData) {
        if (ogData.equals(noisyData)) {
            errors = false;
            System.out.println("\nIntegridad verficada y trama correcta!");
        } else {
            errors = true;
            System.out.println("\nIntegridad Incorrecta!");
        }
    }

    public String eliminateParityBits(String data) {
        StringBuilder originalData = new StringBuilder();
        for (int i = 1; i <= data.length(); i++) {
            if ((i & (i - 1)) != 0) {
                originalData.append(data.charAt(i - 1));
            }
        }
        return originalData.toString();
    }

    public String deCodeMessage(String data) {
        if (errors) {
            return "\nError: No se puede decodificar el mensaje!";
        } else {
            StringBuilder originalText = new StringBuilder();
            String[] binaryChunks = data.split("(?<=\\G.{8})");
            for (String chunk : binaryChunks) {
                int asciiValue = Integer.parseInt(chunk, 2);
                originalText.append((char) asciiValue);
            }
            return originalText.toString();
        }
    }

    public void fullReceptor() {
        try {
            String dataReceived = receive();
            String[] dataReceivedSplit = dataReceived.split(",");
            ogData = dataReceivedSplit[0];
            noisyData = dataReceivedSplit[1];
            System.out.println("\nTrama original            -> " + ogData);
            System.out.println("Trama con errores         -> " + noisyData);

            int n = calculateParityBits(ogData) + ogData.length();
            HammingCoding hamming = new HammingCoding(n, ogData.length(), ogData);
            hamming.fullCoding();
            
            try {
                HamingDecoding hammingDecoding = new HamingDecoding(hamming.hammingCode, noisyData);
                hammingDecoding.fullDecoding(hamming);
                compareData(hamming.hammingCode, hammingDecoding.correctData);
                originalData = eliminateParityBits(hammingDecoding.correctData);
                if (!errors) {
                    System.out.println("Trama corregida           -> " + originalData);
                    decodedMessage = deCodeMessage(originalData);
                    System.out.println("Mensaje decodificado      -> " + decodedMessage);
                } else {
                    System.out.println(deCodeMessage(noisyData));
                }
            } catch (Exception e) {
                errors = true;
                decodedMessage = deCodeMessage(noisyData);
                System.out.println(decodedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Hamming2 {

    public static void main(String[] args) {
        String HOST = "127.0.0.1";
        int PORT = 5555;
        // Scanner scanner = new Scanner(System.in);
        // System.out.println("\n----HAMMING----");
        // System.out.println("Que desea hacer?");
        // System.out.println("1. Emisor");
        // System.out.println("2. Receptor");
        // System.out.print("Opcion -> ");
        // int option = scanner.nextInt();
        int option = 1;

        if (option == 1) {
            ArrayList<String> text = new ArrayList<>();
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
                    HammEmisor sender = new HammEmisor(t);
                    sender.fullEmisor(socketClient);
                }

                socketClient.close(); // Close the socket connection after sending all messages
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Mensajes enviados.");
        } else if (option == 2) {
            
            HammReceptor receiver = new HammReceptor();
            receiver.fullReceptor();
            
        } else {
            System.out.println("Opcion Invalida.");
        }

        // scanner.close();
    }
}
