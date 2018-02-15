public class testMonitorThreadSynch implements Runnable {
	final static int SIZE = 5;
	final static int ROUND = 5;
	
	final MonitorThreadSynch gate;
	
	public testMonitorThreadSynch(MonitorThreadSynch gate) {
		this.gate = gate;
	}
	
	public void run() {
		int index = -1;

		for (int round = 0; round < ROUND; ++round) {
			System.out.println("Thread " + Thread.currentThread().getId() + " is WAITING round:" + round);
			try {
				index = gate.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Thread " + Thread.currentThread().getId() + " is leaving round:" + round);
		}
    //System.out.println("-------------------------------\n" + "Permits left: " + gate.block.availableSeats());
	}
	
	public static void main(String[] args) {
		MonitorThreadSynch gate = new MonitorThreadSynch(SIZE);
		Thread[] t = new Thread[SIZE];
		
		for (int i = 0; i < SIZE; ++i) {
			t[i] = new Thread(new testMonitorThreadSynch(gate));
		}
		
		for (int i = 0; i < SIZE; ++i) {
			t[i].start();
		}
    }
}
