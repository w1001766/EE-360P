package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    Map<Integer, Instance> map;			//Map<seq #, Instance> to store proposals
    int seq;
    Object value;
    int numPaxos;
    int[] done;
    
    int highPrepare = -1;
    int highAccept = -1;
    
    Object highAcceptvalue = null;
    
    

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.map = new HashMap<>();
        this.numPaxos = peers.length;
        this.highPrepare = 0;
        this.done = new int[this.numPaxos];
        for(int i = 0; i < this.done.length; i++){
            this.done[i] = -1;
        }
        

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
    	this.seq = seq;
    	this.value = value;
    	Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run(){
        //Your code here
    	if(this.seq < this.Min()) return;
    	this.Proposer(this.seq, this.value);
        
    }
    
    public void Proposer(int seq, Object value) {
//    	while not decided:
//	    choose n, unique and higher than any n seen so far
//	    send prepare(n) to all servers including self
//	    if prepare_ok(n, n_a, v_a) from majority:
//	      v' = v_a with highest n_a; choose own v otherwise
//	      send accept(n, v') to all
//	      if accept_ok(n) from majority:
//	        send decided(v') to all
    	while(true) {
    		Response resp = sendPrepare(seq, value);
    		boolean OK = false;
    		if(resp.ack) {
    			OK = this.sendAccept(seq, resp.num, resp.value);		// will return whether value is the majority
    		}
    		if(OK) {													// if majority is true,
    			this.sendDecide(seq, resp.num, resp.value);				// send decision to all
                break;
    		}
    		
    		retStatus status = this.Status(seq);
            if(status.state == State.Decided){
                break;
            }
    	}
    }
    
    public Response sendPrepare(int seq, Object value){
    	int prepare = Max() + 1;	// Generate a proposal number (Lamport clock)
    	Request req = new Request(seq, prepare, value);
    	
    	int replyNum = -1;
    	Object replyVal = value;
    	int count = 0;
    	
    	for(int i = 0; i < peers.length; i++) {			// Send a "Prepare" proposal to accepters
    		Response resp;								// receive a response back from the accepters
    		if(i == this.me) {
    			resp = this.Prepare(req);
    		}
    		else {
    			resp = this.Call("Prepare", req, i);
    		}
    		if(resp != null && resp.ack) {				// Count up the amount of acknowledged responses
    			count ++;
    			if(resp.num > replyNum) {
    				replyNum = resp.num;
    				replyVal = resp.value;
    			}
    		}
    	}
    	
    	Response resp = new Response();
        if(count >= this.majority()){
            resp.ack = true;
            resp.num = prepare;
            resp.value = replyVal;
        }
        
        return resp;
    }

    public boolean sendAccept(int seq, int proposalNum, Object value) {
    	int count = 0;
    	for(int i = 0; i < this.peers.length; i++){
            Response resp;
            if(i == this.me){
                resp = Accept(new Request(seq, proposalNum, value));
            }
            else{
                resp = this.Call("Accept", new Request(seq, proposalNum, value), i);
            }

            if(resp != null && resp.ack){
                count++;
            }
        }
        return count >= this.majority();
    }
    
    public void sendDecide(int seq, int proposalNum, Object value) {
    	this.mutex.lock();
        try{
            Instance ins = this.map.get(seq);
            ins.v_accept = value;
            ins.state = State.Decided;
            ins.max_accept = proposalNum;
            ins.max_prepare = proposalNum;

        } finally {
            this.mutex.unlock();
        }

        for(int i = 0; i < this.peers.length; i++){
            Response rsp;
            if(i == this.me){
                continue;
            }
            else {
                int doneVal = this.done[this.me];
                rsp = this.Call("Decide", new Request(seq, proposalNum, value, doneVal, this.me), i);
                //call();
            }
        }

    }
    
    // RMI handler
    public Response Prepare(Request req){
        // your code here
    	/**
		acceptor's prepare(n) handler:
		  if n > n_p
		    n_p = n
		    reply prepare_ok(n, n_a, v_a)
		  else
		    reply prepare_reject
    	 */
    	this.mutex.lock();
    	if(!map.containsKey(req.seq)) {
    		Instance ins = new Instance();
    		map.put(req.seq, ins);
    	}
    	Instance ins = map.get(req.seq);
    	
    	Response resp = new Response();
    	if(req.proposalNum > ins.max_prepare){
            // update proposer number
            ins.max_prepare = req.proposalNum;
            // if ok, reply accept num and value
            resp.ack = true;
            resp.num = ins.max_accept;
            resp.value = ins.v_accept;
        }
        else{
            resp.ack = false;
        }
        this.mutex.unlock();
        return resp;
    }

    public Response Accept(Request req){
        // your code here
    	// n_p (highest prepare seen)
    	// n_a, v_a (highest accept seen)
    	/**
    	acceptor's accept(n, v) handler:
		  if n >= n_p
		    n_p = n
		    n_a = n
		    v_a = v
		    reply accept_ok(n)
		  else
		    reply accept_reject
    	 */
    	this.mutex.lock();
        if(!this.map.containsKey(req.seq)){
            Instance ins = new Instance();
            this.map.put(req.seq, ins);
        }
        Instance ins = this.map.get(req.seq);

        Response rsp = new Response();

        if(req.proposalNum >= ins.max_prepare){
            ins.max_accept = req.proposalNum;
            ins.v_accept = req.val;
            ins.max_prepare = req.proposalNum;
            rsp.ack = true;
        }
        else{
            rsp.ack = false;
        }
        this.mutex.unlock();
        return rsp;
    }

    public Response Decide(Request req){
        // your code here
    	this.mutex.lock();
        if(!this.map.containsKey(req.seq)){
            Instance ins = new Instance();
            this.map.put(req.seq, ins);
        }
        Instance ins = this.map.get(req.seq);

        ins.v_accept = req.val;
        ins.max_prepare = req.proposalNum;
        ins.max_accept = req.proposalNum;
        ins.state = State.Decided;

        this.done[req.me] = req.done;

        Response rsp = new Response();
        this.mutex.unlock();
        return rsp;

    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
    	this.mutex.lock();
        try {
            if (seq > this.done[this.me]) {
                this.done[this.me] = seq;
            }
        }
        finally {
            this.mutex.unlock();
        }
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
    	this.mutex.lock();
    	try{
    		int max = 0;
	        for(int key: map.keySet()) {
	            if(key > max)
	                max = key;
	        }
	        return max;
    	}
    	finally {
            this.mutex.unlock();
    	}
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        // Your code here
    	this.mutex.lock();
        try{
            int min = this.done[this.me];
            for(int i = 0; i < this.done.length; i++){
                if(this.done[i] < min){
                    min = this.done[i];
                }
            }

            for(Iterator<Map.Entry<Integer, Instance>> it = this.map.entrySet().iterator(); it.hasNext();){
                Map.Entry<Integer, Instance> entry = it.next();
                if(entry.getKey() > min){
                    continue;
                }
                if(entry.getValue().state != State.Decided){
                    continue;
                }
                it.remove();
            }

            return min + 1;
        } finally {
            this.mutex.unlock();
        }
    }
    
    /** Returns the majority value
     */
    private int majority(){
        return numPaxos/2 + 1;
    }

    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here
    	if(seq < Min())
    		return new retStatus(State.Forgotten, null);
    	
    	this.mutex.lock();
        try{
            if(!this.map.containsKey(seq)){
                return new retStatus(State.Pending, null);
            }
            else{
                Instance ins = this.map.get(seq);
                return new retStatus(ins.state, ins.v_accept);
            }
        } finally {
            this.mutex.unlock();
        }
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }
    
    private class Instance {

        int max_prepare; // highest prepare number seen
        int max_accept; // highest accept seen
        State state;
        Object v_accept; //highest-numbered proposal accepted

        public Instance(){
            this.max_prepare = Integer.MIN_VALUE;
            this.max_accept = Integer.MIN_VALUE;
            this.state = State.Pending;
            this.v_accept = null;
        }

    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }


}
