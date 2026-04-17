import java.io.*;
import java.net.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TCPServer {


// function will take a string s then convert the string to an int and get the result
// split the string in half from a operator value + - / *
// then convert the left and right side of the string into an int
public static int convertString(String s)
{
  s = s.trim();
  s = s.replaceAll("\\s+", "");

  int operatorIndex = -1;
  char operator = ' ';

  // find operator in string
  for (int i = 0; i < s.length(); i++) {
    char c = s.charAt(i);
    if (c == '+' || c == '-' || c == '*' || c == '/') {
      operator = c;
      operatorIndex = i;
      break;
    }
  }

  if (operatorIndex == -1) {
    throw new IllegalArgumentException("Invalid format. Use examples like 1+2 or 1 + 2");
  }

  int left;
  int right;

  // parse numbers
  try {
    left = Integer.parseInt(s.substring(0, operatorIndex));
    right = Integer.parseInt(s.substring(operatorIndex + 1));
  } catch (NumberFormatException e) {
    throw new IllegalArgumentException("Invalid numbers in expression");
  }

  // perform operation
  switch (operator) {
    case '+': return left + right;
    case '-': return left - right;
    case '*': return left * right;
    case '/':
      if (right == 0) throw new ArithmeticException("Division by zero");
      return left / right;
    default:
      throw new IllegalArgumentException("Invalid operator");
  }
}

  static AtomicInteger clientCount = new AtomicInteger(0);

  // track connected clients
  static ConcurrentHashMap<Socket, ClientInfo> connectedClients = new ConcurrentHashMap<>();

  // queue to ensure FIFO request processing
  static BlockingQueue<ClientRequest> requestQueue = new LinkedBlockingQueue<>();

  // store client details
  static class ClientInfo {
    String clientName;
    String clientIP;
    int clientPort;
    LocalDateTime connectTime;

    ClientInfo(String clientName, String clientIP, int clientPort, LocalDateTime connectTime) {
      this.clientName = clientName;
      this.clientIP = clientIP;
      this.clientPort = clientPort;
      this.connectTime = connectTime;
    }
  }

  // store client request
  static class ClientRequest {
    Socket socket;
    String clientName;
    String expression;
    DataOutputStream outToClient;
    LocalDateTime requestTime;

    ClientRequest(Socket socket, String clientName, String expression,
                  DataOutputStream outToClient, LocalDateTime requestTime) {
      this.socket = socket;
      this.clientName = clientName;
      this.expression = expression;
      this.outToClient = outToClient;
      this.requestTime = requestTime;
    }
  }

  public static void main(String argv[]) throws Exception {

    ServerSocket welcomeSocket = new ServerSocket(6789);

    // worker thread to process requests in order
    Thread requestWorker = new Thread(() -> {
      while (true) {
        try {
          ClientRequest req = requestQueue.take();

          try {
            int clientResult = TCPServer.convertString(req.expression);
            System.out.println("Server received message from '" + req.clientName + "': " + req.expression + " = " + clientResult);
            req.outToClient.writeBytes("Result: " + clientResult + "\n");
          } catch (ArithmeticException e) {
            req.outToClient.writeBytes("ERROR: " + e.getMessage() + "\n");
          } catch (IllegalArgumentException e) {
            req.outToClient.writeBytes("ERROR: " + e.getMessage() + "\n");
          }

        } catch (Exception e) {
          System.out.println("Error processing request.");
        }
      }
    });

    requestWorker.start();

    try {
      System.out.println("Server is UP and running!");

      while (true) {
        Socket connectionSocket = welcomeSocket.accept();
        clientCount.incrementAndGet();

        // handle each client in separate thread
        new Thread(() -> {
          String clientName = "";
          LocalDateTime connectTime = LocalDateTime.now();

          try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            // read client name
            clientName = inFromClient.readLine();

            connectedClients.put(connectionSocket,
              new ClientInfo(clientName,
              connectionSocket.getInetAddress().getHostAddress(),
              connectionSocket.getPort(),
              connectTime));

            System.out.println("Client '" + clientName + "' connected at " + connectTime);

            // send acknowledgement
            outToClient.writeBytes("ACK: Connection successful\n");

            String clientSentence;

            while (true) {
              clientSentence = inFromClient.readLine();

              // handle disconnect
              if (clientSentence == null || clientSentence.equalsIgnoreCase("quit") || clientSentence.equalsIgnoreCase("exit")) {
                Duration duration = Duration.between(connectTime, LocalDateTime.now());
                System.out.println("Client '" + clientName + "' disconnected. Duration: " + duration.toSeconds() + " sec");
                outToClient.writeBytes("ACK: Connection closed\n");
                break;
              }

              // add request to queue
              requestQueue.put(new ClientRequest(
                connectionSocket,
                clientName,
                clientSentence,
                outToClient,
                LocalDateTime.now()
              ));
            }

          } catch (Exception e) {
            System.out.println("Client '" + clientName + "' connection error.");
          } finally {
            clientCount.decrementAndGet();
            connectedClients.remove(connectionSocket);
            try { connectionSocket.close(); } catch (IOException e) {}
          }
        }).start();
      }

    } finally {
      welcomeSocket.close(); // always close the welcome socket on exit
    }
  }
}