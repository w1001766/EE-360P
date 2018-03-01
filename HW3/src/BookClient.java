import java.util.Scanner;
import java.io.*;
import java.util.*;
public class BookClient {
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
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port
    
    String protocol = "U";

    // Initializing UDP objects
    DatagramPacket sPacket, rPacket;
    byte[] rBuffer;

    // Initializing TCP objects
    Socket tcp;


    try {
        Scanner sc = new Scanner(new FileReader(commandFile));
        InetAddress inet = InetAddress.getByName(hostAddress);
        DatagramSocket datasocket = new DatagramSocket();
        
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");

          if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
            if (tokens[1].equals("U")){
              protocol = "U";
            }
            else if (tokens[1].equals("T")){
              protocal = "T";
            }

          }
          else if (tokens[0].equals("borrow")) {
            // borrow <student-name> <book-name>
            // UDP
            byte[] buffer = new byte[cmd.length()];
            buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inet, udpPort);
            datasocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            String output = new String(rPacket.getData(), 0, rPacket.getLength());
            System.out.println(output);

          } else if (tokens[0].equals("return")) {
            // return <record-id>
            // UDP
            byte[] buffer = new byte[cmd.length()];
            buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inet, udpPort);
            datasocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            String output = new String(rPacket.getData(), 0, rPacket.getLength());
            System.out.println(output);

          } else if (tokens[0].equals("inventory")) {
            // list <student-name>
            // UDP
            byte[] buffer = new byte[cmd.length()];
            buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inet, udpPort);
            datasocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            String output = new String(rPacket.getData(), 0, rPacket.getLength());
            System.out.println(output);

          } else if (tokens[0].equals("list")) {
            // inventory
            // UDP
            byte[] buffer = new byte[cmd.length()];
            buffer = cmd.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, inet, udpPort);
            datasocket.send(sPacket);
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            String output = new String(rPacket.getData(), 0, rPacket.getLength());
            System.out.println(output);

          } else if (tokens[0].equals("exit")) {
            // exit (NO OUTPUT)
            
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    }
  }
}
