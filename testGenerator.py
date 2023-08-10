import random
import string

num_strings_per_length = 625
min_length = 1
max_length = 16

with open("tests.txt", "w") as f:
    for length in range(min_length, max_length + 1):
        for _ in range(num_strings_per_length):
            test_string = ''.join(random.choices(string.ascii_letters, k=length))
            f.write(test_string + "\n")
