# Java compiler
JAVAC = javac
JAVA = java

# Files
SERVER = TCPServer
CLIENT = TCPClient

all: compilem

# Compile both files
compile:
	$(JAVAC) $(SERVER).java
	$(JAVAC) $(CLIENT).java

# Run server
server:
	$(JAVA) $(SERVER)

# Run client
client:
	$(JAVA) $(CLIENT)

# Clean compiled files
clean:
	rm -f *.class