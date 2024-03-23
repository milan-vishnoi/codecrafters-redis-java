import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) throws IOException {
   // Log statements can be printed as below, they will be visible when running tests.
    System.out.println("Logs from your program will appear here!");


        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
          serverSocket = new ServerSocket(port);
          // Since the tester restarts your program quite often, setting SO_REUSEADDR
    //      // ensures that we don't run into 'Address already in use' errors
          serverSocket.setReuseAddress(true);
    //      // Wait for connection from client.
          while(true)
          {
            clientSocket = serverSocket.accept();
            final Socket sc = clientSocket;
            new Thread(() -> {
              try{
                handleCommand(sc);
              }catch(Exception e){
                System.out.println("Received:"+e.getMessage());
              }
            }).start();
          }
        
  }

  private static void handleCommand(Socket clientSocket) {
    OutputStreamWriter out = null;
    BufferedReader in = null;
    try
    {
    out = new OutputStreamWriter(clientSocket.getOutputStream());
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    String line = null;
    Boolean echo = false;
    while((line = in.readLine())!=null)
    {
      if(line.toUpperCase().contains("PING"))
      out.write("+PONG\r\n");
      else if(line.toUpperCase().contains("ECHO"))
    {
       echo = true;
    }
      if(echo && line.matches("[A-Za-z]+\r\n"))
      {
        String echoString = line.replace("\r\n", "");
        out.write("$"+echoString.length()+"\r\n"+echoString+"\r\n");
        echo = false;
      } 
   
      out.flush();
    }
    
    } catch(IOException ex)
    {
      System.out.println("Received:"+ex.getMessage());
    }finally{
      try{
        if(clientSocket != null)
        {
          if(out != null)
          out.close();
  
          if(in != null)
          in.close();
  
          clientSocket.close();
        }
      }catch(IOException e)
      {
        System.out.println("Received:"+e.getMessage());
      }
      
    }
    
  }
}
