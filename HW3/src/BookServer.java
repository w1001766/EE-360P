import java.net.*;
import java.io.*;
import java.util.*;

public class BookServer {
  public static void main (String[] args) throws Exception {
    int tcpPort;
    int udpPort;
    int recordCount = 0;
    HashTable<Integer, Record> recordBook = new HashTable<>();       // Record(id, studentName, bookName)
    HashTable<String, Integer> inventory = new HashTable<>();        // bookName, count
    
    public class Record{
      int recordId;
      String student;
      String book;

      Record(int id, String st, String b){
        recordId = id;
        student = st;
        book = b;
      }
    }


    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;
    
    // Initialize the library by parsing input.txt
    try{
      while(sc.hasNextLine()){ 
        Scanner sc = new Scanner(new FileReader(fileName));
        String entry = sc.nextLine();
        String[] tokens = entry.split("\"");
        String bookName = tokens[0].trim();
        int count = Integer.parseInt(tokens[1].trim());
        inventory.put(bookName, count);
      }
    }catch(Exception e){
      e.printStackTrace();
    }


    // Accesses library to checkout a book.
    // If the book isn't in the library, return 0
    // If it is, checkout the book from inventory, and update records
    public synchronized int borrowBook (String name, String book) {
      if (!inventory.containsKey(book)) return -1;
      else{
        int count = inventory.get(book);
        if (count == 0) return 0;
        count -= 1;
        inventory.put(book, count);
        recordCount += 1;
        Record r =  new Record(recordCount, name, book);
        recordBook.put(recordCount, r);
        return recordCount;
      }
    }


    // Return a book back to the library inventory.
    // If the recordID doesn't exist, return false (failure)
    // If it does, update the inventory, return true
    public synchronized boolean returnBook (int recordId) {
      if(!recordbook.containsKey(recordId)) return false;
      else{
        Record r = recordBook.get(recordId);
        String bookName = r.book;
        int count = inventory.get(bookName) + 1;
        inventory.put(bookName, count);
        return true;
      }
    }
    
    
    // Return a string containing a list of all borrowed books from a student
    // If the student hasn't checked out any books, return "No record found..."
    // Else, list in the following format: <recordId> "<bookName>" \n ...
    public synchronized String list (String name){
      return null;
    }


    // Return a string containing a list of all available books
    // You should still print out books of 0 quantity, each with \n
    public synchronized String inventory () {
      StringBuilder list = new StringBuilder();
      for (String book : inventory.KeySet()){
        String count = Integer.toString(inventory.get(book));
        list.append("\"" + book + "\"" + " ").append(count + "\n");
      }
      return list.toString();
    }

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

    // TODO: handle request from clients
    }
  }
}
