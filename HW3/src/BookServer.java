import java.net.*;
import java.io.*;
import java.util.*;

public class BookServer extends Thread {
  static int recordCount = 0;
  static Hashtable<Integer, String[]> recordBook = new Hashtable<Integer, String[]>();       // Recordid, [studentName, bookName]
  static Hashtable<String, ArrayList<Integer>> readingList = new Hashtable<String, ArrayList<Integer>>();        // studentName, id[]
  static Map<String, Integer> inventory = Collections.synchronizedMap(new LinkedHashMap<>());  

  private ServerSocket serverSocket = null;

  private class TcpServer extends Thread {
    private Socket clientSocket;

    public TcpServer(Socket clientSocket) {
      this.clientSocket = clientSocket;
      start();
    }

    public void run() {
      BufferedReader clientRequest = null;
      PrintWriter  responseMsg = null;

      // Initialize read and writers for client
      try {
        clientRequest = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        responseMsg = new PrintWriter(clientSocket.getOutputStream(), true);
      } catch (IOException e) {
        e.printStackTrace();
      }
      String inputLn;
      
      // Attempt to execute client request
      try {
        boolean exit = false;
        while (true) {
          while ((inputLn = clientRequest.readLine()) == null) {System.out.println("Waiting for TCP input");}
          System.out.println("TCP input received!");
          System.out.println("inputLn: " + inputLn);
          String[] tokens = inputLn.split(" ");
          String output;
          
          // Set UDP or TCP mode
          if (tokens[0].equals("setmode")) {
            if (tokens[1].equals("U")) {

            } else if (tokens[1].equals("T")) {

            }
          }
          
          // Book is requested to be borrowed
          else if (tokens[0].equals("borrow")) {
            // Parse book to borrow and requesting student
            System.out.println("Borrow processing");
            String[] split = inputLn.split("\"");
            String book = split[1];
            String name = split[0].split(" ")[1];
            System.out.println("Student name: " + name + ", book title: " + book);

            // Borrow success? 
            int recordId = borrowBook(name, book);
            output = recordId == -1 ? "Request Failed - We do not have this book" :
                     recordId ==  0 ? "Request Failed - Book not available" : 
                                      "Your request has been approved, " + recordId +
                                      " " + name + " \"" + book + "\"";
            System.out.println(output);

            // Send response to client
            System.out.println("Sending response: " + output);
            for (String s : output.split("\n")) {
              responseMsg.println(s);
            }
          }
          
          // Book is requested to be returned
          else if (tokens[0].equals("return")) {
            // Can the book be returned?
            System.out.println("Return processing");
            int id = Integer.parseInt(tokens[1].trim());
            output = returnBook(id) ? tokens[1] + " is returned" :
                                                  " not found, no such borrow record";
            
            // Send response to client
            System.out.println("Sending response: " + output);
            for (String s : output.split("\n")) {
              responseMsg.println(s);
            }
          }
          
          // Inventory list
          else if (tokens[0].trim().equals("inventory")) {
            System.out.println("Inventory request processing");
            output = listInventory();

            // Send response to client
            System.out.println("Sending response: " + output);
            for (String s : output.split("\n")) {
              responseMsg.println(s);
            }
          }
          
          // Student list request
          else if (tokens[0].equals("list")) {
            System.out.println("Student list request processing");
            output = list(tokens[1].trim());
            output = output != null ? output : "No record found for " + tokens[1];

            // Send response to client
            System.out.println("Sending response: " + output);
            for (String s : output.split("\n")) {
              responseMsg.println(s);
            }
          }
          
          // Update or write file, client leaving
          else if (tokens[0].trim().equals("exit")) {
            System.out.println("EXITING...");
            exit();
            exit = true;
            break;
          }

          else {
            System.out.println("lol fuck");
          }

        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
    
  public BookServer() throws Exception {
    start();
  }

  public void run() {
    try {
      System.out.println("Running TCP; opening server socket...");
      serverSocket = new ServerSocket(7070);
      Socket s;
      while ((s = serverSocket.accept()) != null) {
        System.out.println("Creating TcpServer");
        new TcpServer(s);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* ------------------------------------------------------------------------------------------------ */
  /* @@@@@@@@@@ SERVER METHODS @@@@@@@@@@ */
  // Accesses library to checkout a book.
  // If the book isn't in the library, return 0
  // If it is, checkout the book from inventory, and update records
  public static synchronized int borrowBook(String name, String book) {
    if (!inventory.containsKey(book)) return -1;  // Book does not exist
    else{
      int count = inventory.get(book);
      if (count == 0) return 0;             // No book copies available -> return 0
      count -= 1;
      inventory.put(book, count);
      recordCount += 1;
      String[] info = {name, book};
      recordBook.put(recordCount, info);            // Add checkout to record book
                                                    // Add to student reading list
      ArrayList<Integer> studentList; 
      if (!readingList.containsKey(name))          // Create new entry if one doesn't exist
        studentList = new ArrayList<Integer>();
      else                                        // Retrieve existing entry
        studentList = readingList.get(name);

      studentList.add(recordCount);               // Update
      readingList.put(name, studentList);
      return recordCount;
    }
  }


  // Return a book back to the library inventory.
  // If the recordID doesn't exist, return false (failure)
  // If it does, update the inventory, return true
  public static synchronized boolean returnBook(int recordId) {
    if(!recordBook.containsKey(recordId)) return false;
    else{
      String[] info = recordBook.get(recordId);
      String name = info[0];
      String book = info[1];
      int count = inventory.get(book) + 1;
      inventory.put(book, count);
      // Remove id from student's arrayList
      ArrayList<Integer> studentRecord = readingList.get(name);
      if(studentRecord.size() == 1){
        readingList.remove(name);
      }
      else{
        studentRecord.remove(recordId);
        readingList.put(name, studentRecord);
      }
      return true;
    }
  }
  
  
  // Return a string containing a list of all borrowed books from a student
  // If the student hasn't checked out any books, return "No record found..."
  // Else, list in the following format: <recordId> "<bookName>" \n ...
  public static synchronized String list(String name){
    if(!readingList.containsKey(name)) return null;
    StringBuilder result = new StringBuilder();
    ArrayList<Integer> studentRecord = readingList.get(name);
    for (int i = 0; i < studentRecord.size(); i++){
      String book = recordBook.get(studentRecord.get(i))[1];
      System.out.println("Book: " + book + ", recordID = " + studentRecord.get(i));
      result.append(Integer.toString(studentRecord.get(i)) + " \"" + book + "\"\n");
    }
    return result.toString().trim();
  }


  // Return a string containing a list of all available books
  // You should still print out books of 0 quantity, each with \n
  public static synchronized String listInventory() {
    StringBuilder list = new StringBuilder();
    for (String book : inventory.keySet()){
      String count = Integer.toString(inventory.get(book));
      System.out.println("Book: " + book + ", count = " + count);
      list.append("\"" + book + "\"" + " ").append(count + "\n");
    }
    System.out.println(list.toString());
    byte[] rbuffer = new byte[4096];
    return list.toString().trim();
  }

  // Write to file "inventory.txt" and close servers
  public static synchronized void exit() throws IOException{
    File f = new File("../src/inventory.txt");
    FileWriter fwriter = new FileWriter(f);
    PrintWriter writer = new PrintWriter(fwriter, true);
    writer.println(listInventory());
//    writer.close();
  }
  /* ------------------------------------------------------------------------------------------------ */
  
  public static void responseUDP(InetAddress ip, int port, String output, DatagramSocket serverSocket) {
    System.out.println("Sending response: " + output);
    byte[] sendData = output.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
    try {
      serverSocket.send(sendPacket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void main (String[] args) throws Exception {
    int tcpPort;
    int udpPort;

    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    //String fileName = "input.txt";
    tcpPort = 7070;
    udpPort = 8080;
    
    // Initialize the library by parsing input.txt
    System.out.println("Initializing library: reading input.txt...");
    try{
      Scanner sc = new Scanner(new FileReader(fileName));
      while(sc.hasNextLine()){ 
        String entry = sc.nextLine();
        String[] tokens = entry.split("\""); // {"", "<book-name>, <count>} since it splits on quotes
        String bookName = tokens[1].trim();
        int count = Integer.parseInt(tokens[2].trim());
        inventory.put(bookName, count);
      }
      sc.close();
    }catch(Exception e){
      e.printStackTrace();
    }


    DatagramSocket serverSocket = new DatagramSocket(udpPort);
    BookServer bs = new BookServer();    
    
    System.out.println("Establishing connection and creating sockets...");
    String protocol = "U";
    while (true) {
      // UDP Method
      
      byte[] receiveData = new byte[1024];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      String data = new String(receivePacket.getData());
      System.out.println("Input received: " + data);
      String[] tokens = data.split(" ");
      
      String output;
      InetAddress ip = receivePacket.getAddress();
      int port = receivePacket.getPort();
	  
      if (tokens[0].equals("setmode")) {
        if (tokens[1].equals("U")){
        }
        else if (tokens[1].equals("T")){
        }
      }
      
      else if (tokens[0].equals("borrow")) {
        // borrow <student-name> <book-name>
        System.out.println("Borrow processing");
        String[] split = data.split("\"");
        String book = split[1];
        String name = split[0].split(" ")[1];
        System.out.println("Student name: " + name + ", book title: " + book);
        int recordId = borrowBook(name, book);
        if(recordId == -1) output = "Request Failed - We do not have this book";
        else if (recordId == 0) output = "Request Failed - Book not available";
        else output = "Your request has been approved, " + recordId + " " + name + " \"" + book + "\"";
        System.out.println(output);
        if (protocol.equals("T")){
        }
        else{
          responseUDP(ip, port, output, serverSocket);        		
        }
      }
      
      else if (tokens[0].equals("return")) {
        // return <record-id>
    	  System.out.println("Return processing");
        int id = Integer.parseInt(tokens[1].trim());
        if(returnBook(id)) output = tokens[1] + " is returned";
        else output = tokens[1] + " not found, no such borrow record";
        
        if (protocol.equals("T")){
        }
        else{
          responseUDP(ip, port, output, serverSocket);
        }
      }
      
      else if (tokens[0].trim().equals("inventory")) {
        // inventory
        System.out.println("Inventory request processing");
        output = listInventory();
        if (protocol.equals("T")){
        }
        else{
          responseUDP(ip, port, output, serverSocket);
        }
      }
      
      else if (tokens[0].equals("list")) {
        // list <student-name>
    	  	System.out.println("Student list request processing");
  	  	output = list(tokens[1].trim());
  	  	if(output == null) output = "No record found for " + tokens[1];
  	  	
  	  	if (protocol.equals("T")){
  	    }
  	    else{
  	      responseUDP(ip, port, output, serverSocket);
  	    }
      }
      
      else if (tokens[0].trim().equals("exit")) {
    	  System.out.println("EXITING...");
    	  exit();
      } 
      
      else {
        System.out.println("Something fucked up bro, lmao");
      }
    }
  }
}
