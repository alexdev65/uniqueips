The file with the main method to run is CountUniqueIPs.java. 
Provide a single argument: a file with IPs. 
The file must have the following format: lines are separated by LF, and each line contains an IP address like 1.2.3.4 without any additional characters.

There are several implementations of some of the classes and methods. 
Some of the most optimized implementations don't check the format correctness. There are also some settings that can be configured in the main file, such as bufferSize, numThreads, etc.

The main idea is to store IP addresses in memory as bits. The current version uses manually manipulated bits in long variables; there's also a version using BitSet (which is strangely a bit slower). 
For the sake of faster processing, the input file is accessed via memory mapping, and multiple threads run in parallel. Thus, most of the code is dedicated to multi-threading and file reading.

There are 2^32 possible IP addresses (some of them are reserved for specific purposes, but that doesn't matter). With 1 bit per IP, we need to store 2^32 / 8 = 512 MB in memory. 
If we assume a large number of random IP addresses, then 512 MB is the minimum possible memory size for the program. 
If the number of IP addresses is either very low or very high, or if they aren't random, then it's possible to use less memory to store them. 
There are many options to consider in this case. It depends on which cases we want to optimize.

The current implementation simply allocates memory in blocks related to /16 subnets, which only saves memory initially when the number of IPs is low. 
If further optimizations are required, it makes sense to discuss the requirements and possible options.

The initial version should not be considered production-ready. It's more of an experimental setup: some lines are commented, some code is unused, and there are no tests, etc.
