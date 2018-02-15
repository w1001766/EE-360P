public class ReaderWriterTest {

    public static FairReadWriteLock lock = new FairReadWriteLock();

    public static void main(String[] args){

        try {
            Reader reader1 = new Reader(1);
            Reader reader2 = new Reader(2);
            Writer writer1 = new Writer(1);
            Reader reader3 = new Reader(3);
            Writer writer2 = new Writer(2);
            Writer writer3 = new Writer(3);
            Reader reader4 = new Reader(4);
            Reader reader5 = new Reader(5);
            Writer writer4 = new Writer(4);//this should wait for the other to finish
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

class Reader implements  Runnable{
    int ID;

    public Reader(int i){
        this.ID = i;
        new Thread(this).start();
    }

    public void run(){
        try{
            ReaderWriterTest.lock.beginRead(ID);
            System.out.println("Reader " + ID + " has the lock");
            Thread.sleep(1000);
            System.out.println("Reader " + ID + " has just finished");
            ReaderWriterTest.lock.endRead(ID);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class Writer implements Runnable {
    int ID;

    public Writer(int i){
        this.ID = i;
        new Thread(this).start();
    }

    public void run(){
        try{
            ReaderWriterTest.lock.beginWrite(ID);
            System.out.println("Writer " + ID + " has the lock");
            Thread.sleep(1000);
            System.out.println("Writer " + ID + " has just finished");
            ReaderWriterTest.lock.endWrite(ID);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
