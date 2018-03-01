import java.net.*;
import java.io.*;

public class BookServer {
  public static void main (String[] args) throws Exception {
    int tcpPort;
    int udpPort;

    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;
    
    DatagramSocket serverSocket = new DatagramSocket(8000);

    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];

    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      System.out.println("Server received: " + receivePacket.getData());
      InetAddress ip = receivePacket.getAddress();
      int port = receivePacket.getPort();
      String ackMessage = "Received packet";
      sendData = ackMessage.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
      serverSocket.send(sendPacket);

    // parse the inventory file

    // TODO: handle request from clients
    }
  }
}
