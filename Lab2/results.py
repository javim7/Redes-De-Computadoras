import matplotlib.pyplot as plt

def plot_results(algorithm):
    error_counts = [0] * 17
    total_counts = [0] * 17

    with open(f"{algorithm}/{algorithm}Results.txt", "r") as f:
        for line in f:
            parts = line.strip().split()
            result = parts[0]
            length = int(parts[1])
            total_counts[length] += 1
            if result == "Error":
                error_counts[length] += 1

    error_rates = [error_counts[i] / total_counts[i] if total_counts[i] > 0 else 0 for i in range(17)]

    plt.plot(range(17), error_rates, label=algorithm)

plot_results("CheckSum")
plot_results("Hamming")

plt.xlabel("String Length")
plt.ylabel("Error Rate")
plt.legend()

plt.show()
