This project aims to generate C/S mode workload using just TCP.

The client tries to generate requests following a Poisson process, and opens a new thread for each new request. The request seeks to download a fixed-size file from the server.

The server accepts the requests and opens a new thread to transfer the requested file to the client.

Each fixed-size file represents a fixed-size flow.

At the current stage, we just provide three files of different sizes, namely "small", "medium", "large". And one can add more files to represent a certain distribution of flows according your own needs.