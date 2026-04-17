# Java compiler
JAVAC = javac
JAVA = java

# Files
SERVER = TCPServer
CLIENT = TCPClient

# Default target
all: compile

# Compile both files
compile:
	$(JAVAC) $(SERVER).java $(CLIENT).java

# Run server
server: compile
	$(JAVA) $(SERVER)

# Run client (supports args: name host port)
client: compile
	$(JAVA) $(CLIENT) user 127.0.0.1 6789

# Clean compiled files
clean:
	rm -f *.class