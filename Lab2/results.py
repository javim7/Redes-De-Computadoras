import matplotlib.pyplot as plt

def plot_results(algorithm):
    
    max_length = 128
    error_counts = [0] * (max_length + 1)
    
    total_counts = [0] * (max_length + 1)

    with open(f"{algorithm}/{algorithm}Results.txt", "r") as f:
        for line in f:
            line = line.strip()
            if not line: 
                continue
            parts = line.split()
            result = parts[0]
            length = int(parts[-1])
            if 0 <= length <= max_length: # Check if length is within valid range
                total_counts[length] += 1
                if result == "Error:":
                    error_counts[length] += 1

    error_rates = [error_counts[i] / total_counts[i] if total_counts[i] > 0 else 0 for i in range(max_length + 1)]

    # Generate a graph of error rate vs string length
    plt.figure()
    plt.plot(range(max_length + 1), error_rates, label=algorithm)
    plt.xlabel("String Length")
    plt.ylabel("Error Rate")
    plt.title(f"Error Rate vs String Length for {algorithm}")
    plt.legend()
    plt.show()

    return sum(error_counts)

# Plot results for both algorithms
hamming_errors = plot_results("Hamming")
checksum_errors = plot_results("CheckSum")

# Generate a bar graph comparing the total number of errors for each algorithm
plt.figure()
plt.bar(["Hamming", "CheckSum"], [hamming_errors, checksum_errors])
plt.xlabel("Algorithm")
plt.ylabel("Total Errors")
plt.title("Total Errors for Hamming and CheckSum Algorithms")
plt.show()
