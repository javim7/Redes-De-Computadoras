import java.util.Arrays;

class HammingCoding{

    private String data;
    private int totalBits;
    private int parityBitCount;
    public String hammingCode;
    public String parityBits;

     public HammingCoding(int n, int m, String dataSent) {
        int r = n - m;
        if ((m + r + 1) <= Math.pow(2, r)) {
            this.data = dataSent;
        } else {
            throw new IllegalArgumentException("Codigo invalido, no cumple con la siguiente condicion: (m + r + 1) <= 2^r");
        }
    }

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

    private int calculateParity(int parityIndex, String[] hammingCode) {
        int parityValue = 0;
        for (int i = 1; i <= this.totalBits; i++) {
            // Check if the bit at i should be used for calculating the parity
            if ((i & (parityIndex)) != 0) {
                if (hammingCode[i].startsWith("P")) {
                    continue; // Skip parity bits with labels "P1", "P2", etc.
                }
                int bit = Integer.parseInt(hammingCode[i]);
                parityValue ^= bit;
            }
        }
        return parityValue;
    }      

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

    public void fullCoding() {
        System.out.println("\nTrama ingresada: " + this.data);
        System.out.println("Numero de bits de paridad: " + calculateParityBits());
        String[] hammingCodeArray = definePositions();
        this.hammingCode = getCode(hammingCodeArray)[0];
        this.parityBits = getCode(hammingCodeArray)[1];
        System.out.println("Codigo: " + this.hammingCode);
        System.out.println("Bits de paridad: " + this.parityBits);
    }

}

class HamingDecoding {

    private String data;
    public String wrongData;
    public String dataWithoutParityBits;
    public String errorBits;
    public String parityBits;
    public int errorPosition;
    public String correctData;

    public HamingDecoding(String data, String wrongData) {
        this.data = data;
        this.wrongData = wrongData;
        System.out.println("\nTrama recibida: " + this.data);
        System.out.println("Trama con errores recibida: " + this.wrongData);
    }

    public String removeParityBits() {
        StringBuilder dataWithoutParityBits = new StringBuilder();
        for (int i = 0; i < this.wrongData.length(); i++) {
            if ((i + 1) != Math.pow(2, Math.floor(Math.log(i + 1) / Math.log(2)))) {
                dataWithoutParityBits.append(this.wrongData.charAt(i));
            } else {
                this.parityBits += this.wrongData.charAt(i);
            }
        }
        
        return dataWithoutParityBits.toString();
    }

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

    public int bitsToDecimals() {
        int decimal = 0;
        StringBuilder reversedErrorBits = new StringBuilder(this.errorBits).reverse();
        for (int i = 0; i < reversedErrorBits.length(); i++) {
            if (reversedErrorBits.charAt(i) == '1') {
                decimal += Math.pow(2, i);
            }
        }
        return decimal;
    }

    public String correctErrors() {
        int errorCount = 0;
        String correct;
        int errorBitIndex = -1;
        for (int i = 0; i < this.errorBits.length(); i++) {
            if (this.errorBits.charAt(i) == '1') {
                errorCount++;
                errorBitIndex = i;
            }
        }

        if (errorCount == 0) {
            System.out.println("\nNo errors found.");
            return this.wrongData;
        } else if (errorCount == 1) {
            StringBuilder reversedErrorBits = new StringBuilder(this.errorBits).reverse();
            int parityBitPosition = reversedErrorBits.indexOf("1");
            char correctedParityBit = this.parityBits.charAt(parityBitPosition);
            correct = this.parityBits.substring(0, parityBitPosition) + (correctedParityBit == '0' ? '1' : '0') + this.parityBits.substring(parityBitPosition + 1);
            System.out.println("Se corrigió el bit de paridad en la posición " + (parityBitPosition + 1));
            return correct;
        } else {
            int dataBitPosition = bitsToDecimals();
            StringBuilder stringBuilder = new StringBuilder(this.wrongData);
            char bitValue = this.wrongData.charAt(dataBitPosition - 1);
            stringBuilder.setCharAt(dataBitPosition - 1, bitValue == '0' ? '1' : '0');

            correct = stringBuilder.toString();
            System.out.println("Errores encontrados, se corrigió el bit en la posición " + (dataBitPosition));
            return correct;
        }
    }

}

class Main{

    public static void main(String[] args) {
        HammingCoding hamming = null;
        // codificacion
       try {
            hamming = new HammingCoding(11, 7, "0111011");
            hamming.fullCoding();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        // decodificacion
        HamingDecoding hammingDecoding = new HamingDecoding(hamming.hammingCode, "10011110011");
        hammingDecoding.dataWithoutParityBits = hammingDecoding.removeParityBits();
        System.out.println("Trama sin bits de paridad: " + hammingDecoding.dataWithoutParityBits);
        HammingCoding haming2 = new HammingCoding(11, 7, hammingDecoding.dataWithoutParityBits);
        haming2.fullCoding();
        hammingDecoding.errorBits = hammingDecoding.compareParityBits(hamming.parityBits, haming2.parityBits);
        System.out.println("\nBits de error: " + hammingDecoding.errorBits);
        hammingDecoding.correctData = hammingDecoding.correctErrors();
        System.out.println("Trama corregida: " + hammingDecoding.correctData);
    }
    
}