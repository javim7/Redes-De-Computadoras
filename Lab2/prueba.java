public class prueba {
    public double calculateNoiseProbability(String text) {
        int length = text.length();
        double errorsInInterval = length/2;

        double baseProb = 1 / errorsInInterval; //0.25
        double adjustment = baseProb / 2; //0.125

        double randomValue = Math.random();
        if (randomValue < 0.33) {
            return baseProb - adjustment;
        } else if (randomValue < 0.66 && randomValue >= 0.33) {
            return baseProb + adjustment;
        } else {
            return baseProb;
        }
    }

    public String addNoise(String text) {
        double noiseProbability = calculateNoiseProbability(text);
        System.out.println("Probabilidad de ruido: " + noiseProbability);
        return text;
    }

    public static void main(String[] args) {
        prueba p = new prueba();
        String text = "01010101";
        System.out.println("Texto original: " + text);
        System.out.println("Texto con ruido: " + p.addNoise(text));
    }
}
