import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
  public static void requestUDP(String cmd, DatagramSocket datasocket, InetAddress inet, int port, PrintWriter pwriter) throws IOException {
	  byte[] buffer = new byte[cmd.length()];
    byte[] rbuffer = new byte[4096];
    buffer = cmd.getBytes();
    DatagramPacket sPacket = new DatagramPacket(buffer, cmd.length(), inet, port);
    datasocket.send(sPacket);
    DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
    datasocket.receive(rPacket);
    String output = new String(rPacket.getData(), 0, rPacket.getLength());
    System.out.println(output);
    pwriter.println(output);
  }

  public static void requestTCP(String cmd, Socket tcpSocket, PrintWriter pwriter) throws Exception {
    PrintWriter request = new PrintWriter(tcpSocket.getOutputStream(), true);
    BufferedReader response = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
    request.println(cmd);
    String serverResponse;
    //while ((serverResponse = response.readLine()) == null || serverResponse == "") {System.out.println("waiting...");}
    //System.out.println(serverResponse);
    if ((serverResponse = response.readLine().trim()) != null) {
      serverResponse = serverResponse.replaceAll("}", "\n");
      System.out.println(serverResponse);
      pwriter.println(serverResponse);
    }
    /*
    do {
      System.out.println(serverResponse=="");
      System.out.println(serverResponse);
    } while ((serverResponse = response.readLine()) != null && serverResponse != "");*/
  }
  
  public static void main (String[] args) throws Exception {
    String hostAddress;
    int tcpPort;
    int udpPort;
    String clientId = args[1];

    String fileName   = "out_" + clientId + ".txt";
    String fileOutput = "";
    
    File outputFile = new File(fileName);
    FileWriter fwriter = new FileWriter(outputFile);
    PrintWriter pwriter = new PrintWriter(fwriter, true);


    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    hostAddress = "localhost";
    tcpPort = 7070;// hardcoded -- must match the server's tcp port
    udpPort = 8080;// hardcoded -- must match the server's udp port
    
    String protocol = "U";

    // Initializing UDP objects
    DatagramPacket sPacket, rPacket;

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));    // change System.in to new FileReader(commandFile)
        InetAddress inet = InetAddress.getByName(hostAddress);
        DatagramSocket datasocket = new DatagramSocket();
        Socket tcpSocket = new Socket(hostAddress, tcpPort);

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          
          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
            if (tokens[1].equals("U")){
              protocol = "U";
            }
            else if (tokens[1].equals("T")){
              protocol = "T";
            }
          }
          
          else if (tokens[0].equals("borrow")) {
            // borrow <student-name> <book-name>
            //TCP
            if (protocol.equals("T")){
              requestTCP(cmd, tcpSocket, pwriter);
            }
            else{
            	  requestUDP(cmd, datasocket, inet, udpPort, pwriter);
            }
          }
          
          else if (tokens[0].equals("return")) {
            // return <record-id>
        	    // TCP
        	    if (protocol.equals("T")){
              requestTCP(cmd, tcpSocket, pwriter);
        	    }
        	    else{
                requestUDP(cmd, datasocket, inet, udpPort, pwriter);
        	    }
          }
          
          else if (tokens[0].trim().equals("inventory")) {
            // list <student-name>
      	    // TCP
      	    if (protocol.equals("T")){
              requestTCP(cmd, tcpSocket, pwriter);
      	    }
      	    else{
      	      requestUDP(cmd, datasocket, inet, udpPort, pwriter);
      	    }
          }
          
          else if (tokens[0].equals("list")) {
            // inventory
      	    // TCP
      	    if (protocol.equals("T")){
              requestTCP(cmd, tcpSocket, pwriter);
      	    }
      	    else{
      	      requestUDP(cmd, datasocket, inet, udpPort, pwriter);
      	    }
          }
          
          else if (tokens[0].equals("exit")) {
            // exit (NO OUTPUT)
            // Send info to server to close
      	    // TCP
            
            PrintWriter request = new PrintWriter(tcpSocket.getOutputStream(), true);
            request.println(cmd);
            request.close();

            byte[] buffer = new byte[cmd.length()];
            buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, cmd.length(), inet, udpPort);
            datasocket.send(sPacket);
            datasocket.close(); 
            sc.close();
            System.exit(0);
            fwriter.close();
            pwriter.close();
            break;
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    }
    catch (Exception e) {
	    e.printStackTrace();
    }
  }
}
