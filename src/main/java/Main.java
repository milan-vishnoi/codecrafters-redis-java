import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {
  public static void main(String[] args) throws IOException {
    // Log statements can be printed as below, they will be visible when running
    // tests.
    System.out.println("Logs from your program will appear here!");

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 6379;
    serverSocket = new ServerSocket(port);
    // Since the tester restarts your program quite often, setting SO_REUSEADDR
    // // ensures that we don't run into 'Address already in use' errors
    serverSocket.setReuseAddress(true);
    // // Wait for connection from client.
    while (true) {
      clientSocket = serverSocket.accept();
      final Socket sc = clientSocket;
      new Thread(() -> {
        try {
          handleCommand(sc);
        } catch (Exception e) {
          System.out.println("Received:" + e.getMessage());
        }
      }).start();
    }

  }

  private static void handleCommand(Socket clientSocket) {
    OutputStreamWriter out = null;
    BufferedReader in = null;
    try {
      out = new OutputStreamWriter(clientSocket.getOutputStream());
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String line = null;
      line = in.readLine();
      if (line.startsWith("*")) {
        int noOfLines = Integer.parseInt(line.substring(1));
        System.out.println("Number of lines:" + noOfLines);
        HashMap<String, String> values = new HashMap<>();
        HashMap<String, Long> validDuration = new HashMap<>();
        while (true) {
          if ((line = in.readLine()).startsWith("$")) {
            int commandLength = Integer.parseInt(line.substring(1));
            String command = in.readLine().substring(0, commandLength);
            if (command.equalsIgnoreCase("PING"))
              out.write("+PONG\r\n");
            else if (command.equalsIgnoreCase("ECHO"))
              handleEchoCommand(out, in);
            else if (command.equalsIgnoreCase("SET"))
              handleSetCommand(out, in, values, validDuration, noOfLines);
            else if (command.equalsIgnoreCase("GET"))
              handleGetCommand(out, in, values, validDuration);
          }
          out.flush();
        }
      }

    } catch (IOException ex) {
      System.out.println("Received:" + ex.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          if (out != null)
            out.close();

          if (in != null)
            in.close();

          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("Received:" + e.getMessage());
      }

    }

  }

  private static void handleEchoCommand(OutputStreamWriter out, BufferedReader in) throws IOException {
    String line;
    if ((line = in.readLine()).startsWith("$")) {
      int argumentLength = Integer.parseInt(line.substring(1));
      String argument = in.readLine().substring(0, argumentLength);
      out.write("$" + argumentLength + "\r\n" + argument + "\r\n");
    }
  }

  private static void handleGetCommand(OutputStreamWriter out, BufferedReader in, HashMap<String, String> values,
      HashMap<String, Long> validDuration)
      throws IOException {
    String line;
    if ((line = in.readLine()).startsWith("$")) {
      int keyLength = Integer.parseInt(line.substring(1));
      String key = in.readLine().substring(0, keyLength);
      String value = values.get(key);
      if (value == null || value.isEmpty())
        value = "-1";
      else if (validDuration.get(key) < System.currentTimeMillis()) {
        value = "-1";
        values.remove(key);
        validDuration.remove(key);
      }

      if (value.equals("-1"))
        out.write("$" + value + "\r\n");
      else
        out.write("$" + value.length() + "\r\n" + value + "\r\n");
    }
  }

  private static void handleSetCommand(OutputStreamWriter out, BufferedReader in, HashMap<String, String> values,
      HashMap<String, Long> validDuration, int noOfLines)
      throws IOException {
    int argumentNumber = 1;
    String line;
    if ((line = in.readLine()).startsWith("$")) {
      int keyLength = Integer.parseInt(line.substring(1));
      String key = in.readLine().substring(0, keyLength);
      argumentNumber++;
      if ((line = in.readLine()).startsWith("$")) {
        int valueLength = Integer.parseInt(line.substring(1));
        String value = in.readLine().substring(0, valueLength);
        argumentNumber++;
        values.put(key, value);
        validDuration.put(key, Long.MAX_VALUE);
        handleExpiry(in, validDuration, noOfLines, argumentNumber, key);
        out.write("+OK\r\n");
      }
    }
  }

  private static void handleExpiry(BufferedReader in, HashMap<String, Long> validDuration, int noOfLines,
      int argumentNumber,
      String key) throws IOException {
    String line;
    if (argumentNumber < noOfLines && (line = in.readLine()).startsWith("$")) {
      if ((line = in.readLine()).toUpperCase().contains("PX")) {
        argumentNumber++;
        if ((line = in.readLine()).startsWith("$")) {
          int expiryLength = Integer.parseInt(line.substring(1));
          int expiry = Integer.parseInt(in.readLine().substring(0, expiryLength));
          argumentNumber++;
          validDuration.put(key, System.currentTimeMillis() + expiry);
        }
      }
    }
  }
}
