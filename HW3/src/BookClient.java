import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.net.*;

public class BookClient {
  public static void requestUDP(String cmd, DatagramSocket datasocket, InetAddress inet, int port) throws IOException {
	  byte[] buffer = new byte[cmd.length()];
    byte[] rbuffer = new byte[4096];
    buffer = cmd.getBytes();
    DatagramPacket sPacket = new DatagramPacket(buffer, cmd.length(), inet, port);
    datasocket.send(sPacket);
    DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
    datasocket.receive(rPacket);
    String output = new String(rPacket.getData(), 0, rPacket.getLength());
    System.out.println(output);
  }
  
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int clientId;

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }

    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7070;// hardcoded -- must match the server's tcp port
    udpPort = 8080;// hardcoded -- must match the server's udp port
    
    String protocol = "U";

    // Initializing UDP objects
    DatagramPacket sPacket, rPacket;

    // TCP objects and methods
    Socket tcp;

    try {
        Scanner sc = new Scanner(new FileReader(commandFile));    // change System.in to new FileReader(commandFile)
        InetAddress inet = InetAddress.getByName(hostAddress);
        DatagramSocket datasocket = new DatagramSocket();
//        tcp = new Socket(inet, tcpPort);

        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          
          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
            if (tokens[1].equals("U")){
              System.out.println("Switching to UDP");
              protocol = "U";
            }
            else if (tokens[1].equals("T")){
              System.out.println("Switch to TCP");
              protocol = "T";
            }
          }
          
          else if (tokens[0].equals("borrow")) {
            // borrow <student-name> <book-name>
            //TCP
            if (protocol.equals("T")){
            }
            else{
            	  requestUDP(cmd, datasocket, inet, udpPort);
            }
          }
          
          else if (tokens[0].equals("return")) {
            // return <record-id>
        	    // TCP
        	    if (protocol.equals("T")){
        	    }
        	    else{
          	  requestUDP(cmd, datasocket, inet, udpPort);
        	    }
          }
          
          else if (tokens[0].trim().equals("inventory")) {
            // list <student-name>
      	    // TCP
      	    if (protocol.equals("T")){
      	    }
      	    else{
      	      requestUDP(cmd, datasocket, inet, udpPort);
      	    }
          }
          
          else if (tokens[0].equals("list")) {
            // inventory
      	    // TCP
      	    if (protocol.equals("T")){
      	    }
      	    else{
      	      requestUDP(cmd, datasocket, inet, udpPort);
      	    }
          }
          
          else if (cmd.trim().equals("exit")) {
            // exit (NO OUTPUT)
            // Send info to server to close
      	    // TCP
            // sc.close();
      	    if (protocol.equals("T")){
      	    }
      	    else{
	            byte[] buffer = new byte[cmd.length()];
              buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, cmd.length(), inet, udpPort);
              datasocket.send(sPacket);
              datasocket.close(); 
              System.exit(0);
              break;
      	    }
            //tcp.close();
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
