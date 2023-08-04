/**
 * Hamming.java: Es el archivo que implementa el algoritmo de hamming para poder codificar y decodificar una trama de datos.
 * 
 * @version 1.0  
 * @since 01/08/2023
 * @author Javier Mombiela
 * @author Javier Valle
 * 
 */

// import java.util.Arrays;

/**
 * HammingCoding: Es la clase que se encarga de codificar una trama de datos.
 */
class HammingCoding{

    private String data;
    private int totalBits;
    private int parityBitCount;
    public String hammingCode;
    public String parityBits;
    
    /**
     * Constructor de la clase HammingCoding
     * @param n la longitud de la trama de datos mas los bits de paridad
     * @param m la longitud de la trama de datos
     * @param dataSent la trama de datos
     */
    public HammingCoding(int n, int m, String dataSent) {
        int r = n - m;
        if ((m + r + 1) <= Math.pow(2, r)) {
            this.data = dataSent;
        } else {
            throw new IllegalArgumentException("\nCodigo invalido, no cumple con la siguiente condicion: (m + r + 1) <= 2^r");
        }
    }

    /**
     * Metodo que calcula la cantidad de bits de paridad que se necesitan para codificar la trama de datos
     * @return la cantidad de bits de paridad
     */
    public int calculateParityBits() {
        int m = this.data.length();
        int r = 0;

        while (Math.pow(2, r) < m + r + 1) {
            r++;
        }

        this.totalBits = m + r;
        this.parityBitCount = r;
        return r;
    }

    /**
     * Metodo para calcular el valor de un bit de paridad
     * @param parityIndex el indice del bit de paridad
     * @param hammingCode el arreglo que contiene la trama de datos
     * @return el valor del bit de paridad
     */
    public int calculateParity(int parityIndex, String[] hammingCode) {
        int parityValue = 0;
        for (int i = 1; i <= this.totalBits; i++) {
            if ((i & (parityIndex)) != 0) {
                if (hammingCode[i].startsWith("P")) {
                    continue; 
                }
                int bit = Integer.parseInt(hammingCode[i]);
                parityValue ^= bit;
            }
        }
        return parityValue;
    }      

    /**
     * Metodo para definir las posiciones de los bits de paridad
     * @return un arreglo con las posiciones de los bits de paridad
     */
    public String[] definePositions() {
        String[] hammingCodeArray = new String[this.totalBits + 1];
    
        for (int i = 1, j = 0; i <= this.totalBits; i++) {
            if (i == Math.pow(2, j)) {
                hammingCodeArray[i] = "P" + (j + 1);
                j++;
            } else {
                hammingCodeArray[i] = this.data.charAt(i - j - 1) + "";
            }
        }
    
        // System.out.println(Arrays.toString(hammingCodeArray));
    
        for (int j = 0; j < this.parityBitCount; j++) {
            int parityIndex = (int) Math.pow(2, j);
            int parityValue = calculateParity(parityIndex, hammingCodeArray);
            hammingCodeArray[parityIndex] = "(" + parityValue + ")";
        }
    
        // System.out.println(Arrays.toString(hammingCodeArray));
        return hammingCodeArray;
    }    

    /**
     * Metodo para obtener el codigo de hamming y los bits de paridad
     * @param hammingCodeArray el arreglo que contiene la trama de datos
     * @return un arreglo con el codigo de hamming y los bits de paridad
     */
    public String[] getCode(String[] hammingCodeArray) {
        StringBuilder hammingCode = new StringBuilder();
        StringBuilder bitsInsideParentheses = new StringBuilder();
        for (int i = 1; i < hammingCodeArray.length; i++) {
            if (hammingCodeArray[i] != null) {
                hammingCode.append(hammingCodeArray[i]);
                if (hammingCodeArray[i].startsWith("(") && hammingCodeArray[i].endsWith(")")) {
                    bitsInsideParentheses.append(hammingCodeArray[i].substring(1, hammingCodeArray[i].length() - 1));
                }
            }
        }
        // return new String[]{hammingCode.toString(), bitsInsideParentheses.toString()};
        return new String[]{hammingCode.toString().replaceAll("[()]", ""), bitsInsideParentheses.toString()};
    }

    /**
     * Metodo para codificar la trama de datos
     */
    public void fullCoding() {
        System.out.println("Trama ingresada           -> " + this.data);
        System.out.println("Numero de bits de paridad -> " + calculateParityBits());
        String[] hammingCodeArray = definePositions();
        this.hammingCode = getCode(hammingCodeArray)[0];
        this.parityBits = getCode(hammingCodeArray)[1];
        System.out.println("Bits de paridad           -> " + this.parityBits);
        System.out.println("Codigo                    -> " + this.hammingCode);
    }

}

/**
 * HamingDecoding: Es la clase que se encarga de decodificar una trama de datos.
 */
class HamingDecoding {

    private String data;
    public String wrongData;
    public String dataWithoutParityBits;
    public String errorBits;
    public String parityBits = "";
    public int errorPosition;
    public String correctData;

    /**
     * Constructor de la clase HamingDecoding
     * @param data la trama de datos correcta
     * @param wrongData la trama de datos con errores
     */
    public HamingDecoding(String data, String wrongData) {
        this.data = data;
        this.wrongData = wrongData;
        System.out.println("Trama recibida            -> " + this.data);
        System.out.println("Trama con errores         -> " + this.wrongData);
    }

    /**
     * Metodo para remover los bits de paridad de la trama de datos
     * @return la trama de datos sin los bits de paridad
     */
    public String removeParityBits() {
        StringBuilder dataWithoutParityBits = new StringBuilder();
        for (int i = 0; i < this.wrongData.length(); i++) {
            if ((i + 1) != Math.pow(2, Math.floor(Math.log(i + 1) / Math.log(2)))) {
                dataWithoutParityBits.append(this.wrongData.charAt(i));
            } else {
                this.parityBits += this.wrongData.charAt(i);
            }
        }
        // System.out.println("Bits de paridad recibidos: " + this.parityBits);
        return dataWithoutParityBits.toString();
    }

    /**
     * Metodo para comparar los bits de paridad de la trama de datos correcta y la trama de datos con errores
     * @param parity1 los bits de paridad de la trama correcta
     * @param parity2 los bits de paridad de la trama con errores
     * @return los bits de error
     */
    public String compareParityBits(String parity1, String parity2) {
        String errorBits = "";
        for (int i = 0; i < parity1.length(); i++) {
            if (parity1.charAt(i) != parity2.charAt(i)) {
                errorBits += "1";
            } else {
                errorBits += "0";
            }
        }
        // Reverse the errorBits string
        StringBuilder reversedErrorBits = new StringBuilder(errorBits).reverse();
        return reversedErrorBits.toString();
    }

    /**
     * Metodo para convertir los bits de error de binario a decimal
     * @return los bits de error en decimal
     */
    public int binaryToDecimals() {
        int decimal = 0;
        StringBuilder reversedErrorBits = new StringBuilder(this.errorBits).reverse();
        for (int i = 0; i < reversedErrorBits.length(); i++) {
            if (reversedErrorBits.charAt(i) == '1') {
                decimal += Math.pow(2, i);
            }
        }
        return decimal;
    }

    /**
     * Metodo para corregir los errores de la trama de datos
     * @param originalParity los bits de paridad de la trama correcta
     * @return la trama de datos corregida
     */
    public String correctErrors(String originalParity) {
        int errorCount = 0;
        StringBuilder parityCompared = new StringBuilder(compareParityBits(originalParity, this.parityBits)).reverse();
        // System.out.println("\nBits de paridad originales: " + this.parityBits);
        // System.out.println("Bits de paridad recibidos: " + originalParity);
        // System.out.println("parity compared: " + parityCompared);
        String correct;
        int errorBitIndex = -1;

        for (int i = 0; i < this.errorBits.length(); i++) {
            if (this.errorBits.charAt(i) == '1') {
                errorCount++;
                // errorBitIndex = i;
            }
        }

        for(int i = 0; i < parityCompared.length(); i++) {
            if (parityCompared.charAt(i) == '1') {
                errorCount++;
                errorBitIndex = i;
            }
        }
        // System.out.println("errorCount: " + errorCount);
        // System.out.println("errorBitIndex: " + errorBitIndex);

        if (errorCount == 0) {
            System.out.println("\nNo se detectaron errores.");
            correct = this.wrongData;
        } else if (errorCount == 1) {
            int wrongPosition = (int) Math.pow(2, errorBitIndex);
            StringBuilder stringBuilder = new StringBuilder(this.wrongData);
            char bitValue = this.wrongData.charAt(wrongPosition - 1);
            stringBuilder.setCharAt(wrongPosition - 1, bitValue == '0' ? '1' : '0');

            correct = stringBuilder.toString();
            System.out.println("\nSe detectó un error, se corrigió el bit de paridad en la posición " + wrongPosition);
        } else {
            int dataBitPosition = binaryToDecimals();
            StringBuilder stringBuilder = new StringBuilder(this.wrongData);
            char bitValue = this.wrongData.charAt(dataBitPosition - 1);
            stringBuilder.setCharAt(dataBitPosition - 1, bitValue == '0' ? '1' : '0');

            correct = stringBuilder.toString();
            System.out.println("\nSe detectaron varios errores, se corrigió el bit en la posición " + (dataBitPosition));
        }
        return correct;
    }

    /**
     * Metodo para decodificar la trama de datos
     * @param hamming la trama de datos codificada
     */
    public void fullDecoding(HammingCoding hamming) {
        this.dataWithoutParityBits = this.removeParityBits();
        // System.out.println("Trama sin bits de paridad -> " + this.dataWithoutParityBits);
        HammingCoding haming2 = new HammingCoding(11, 7, this.dataWithoutParityBits);
        haming2.fullCoding();
        this.errorBits = this.compareParityBits(hamming.parityBits, haming2.parityBits);
        // System.out.println("\nBits de error: " + this.errorBits);
        this.correctData = this.correctErrors(hamming.parityBits);
        System.out.println("Trama original  -> " + this.data);
        System.out.println("Trama corregida -> " + this.correctData);
    }

}

/**
 * Hamming: Es la clase que se encarga de ejecutar el programa.
 */
class Hamming{

    /**
     * Metodo main para poder controlar el programa
     * @param args
     */
    public static void main(String[] args) {
        HammingCoding hamming = null;
        // codificacion
       try {
           hamming = new HammingCoding(38, 32, "10011100");
            System.out.println("\n--------CODIFICACION--------");
            hamming.fullCoding();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        // decodificacion
        try {
            System.out.println("\n--------DECODIFICACION--------");
            HamingDecoding hammingDecode = new HamingDecoding(hamming.hammingCode, "111100101011");
            hammingDecode.fullDecoding(hamming);
        } catch(Exception e) {
            System.out.println("\nSe ha detectado mas de un error.");
            return;
        }
    }
    
}