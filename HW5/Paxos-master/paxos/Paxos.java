package paxos;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
import java.util.*;
/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock lock = new ReentrantLock();
    Semaphore mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Map<seq, Paxos Instance>
    Map<Integer, Instance> instances = new ConcurrentHashMap<Integer, Instance>();
    Map<Integer, Object> defaultVals = new ConcurrentHashMap<Integer, Object>();

    int seq;
    int[] dones;
    int npaxos;
    int defaultProposalNum = 0;

    State state = State.Pending;
    Object defaultValue;

    private class Instance{
    	
    	int highestProposal;	// n_p
    	int highestAccepted;	// n_a
    	State state;
    	Object value;			// v_a
    	
    	public Instance() {
    		highestProposal = Integer.MIN_VALUE;
    		highestAccepted = Integer.MIN_VALUE;
    		state = State.Pending;
    		value = null;
    	}

    	@Override
        public String toString() {
    	    String strVal;
    	    try {
                strVal = (String)this.value;
            } catch (ClassCastException e) {
    	        strVal = Integer.toString((int)this.value);
            }

    	    String str = (
    	            "Instance values:\n" +
                            "\thighestProposal: " + this.highestAccepted + "\n" +
                            "\thighestAccepted: " + this.highestAccepted + "\n" +
                            "\tstate: " + this.state + "\n" +
                            "\tvalue: " + strVal + "\n"
                    );
    	    return str;
        }
    	
    }
    
    // This method retrieves the instance class associated with a specific sequence
    // in order to run paxos concurrently.
    private Instance getInstance(int seq) {
    	lock.lock();
    	if(!instances.containsKey(seq)) {
    		Instance instance = new Instance();
    		instances.put(seq, instance);
    	}
    	lock.unlock();
    	return instances.get(seq);
    	
    }
    
    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        //this.mutex = new ReentrantLock();
        this.mutex = new Semaphore(1);
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.npaxos = peers.length;
        //this.seq = -1;
        this.defaultValue = null;
        this.dones = new int[npaxos];
        for(int i = 0; i < npaxos; i++)
        	dones[i] = -1;

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
    	this.defaultValue = value;
    	defaultVals.put(seq, value);

    	Thread t = new Thread(this);
    	t.start();
    }

    @Override
    public void run(){
        int currentSeq = this.seq;
        Object currentVal = defaultVals.get(currentSeq);
        if(this.seq < this.Min()) return;

        /* While the Paxos instance has not decided on an entity */
        while (this.getInstance(currentSeq).state != State.Decided) {
            //int proposalNum = this.defaultProposalNum;

            // Propose step
            Response proposalResponse = sendProposal(currentSeq, currentVal);

            
            // Accept step
            if (proposalResponse.majorityAccepted) {
            	Request acceptRequest = new Request(currentSeq, proposalResponse.proposalNum, proposalResponse.value);
                boolean consensus = sendAcceptRequests(acceptRequest);
                // Decide step
                if (consensus) {
                    sendDecideRequests(acceptRequest);
                    break;
                }
            }
        }
    }

    Request generatePrepareRequest(int seq, Object value, Instance instance) {
    	return new Request(
    	        seq,
                instance.highestProposal == Integer.MIN_VALUE ?
                        this.me+1 : (instance.highestProposal / this.npaxos+1) * this.npaxos + this.me+1,
                value
        );
    }
    
    
    public Response sendProposal(int seq, Object value) {
        Instance instance = this.getInstance(seq);
        Object vPrime = value;			// proposer's default value
        int n_a = instance.highestProposal;				// highest n_a (to be calculated), see line 6 from pseudo code
        
        Request prepareRequest = generatePrepareRequest(seq, value, instance);
        int generatedProposalNum = prepareRequest.proposalNum;
        int numAccepted = 0;

    	// Iterate through peers to send prepare message via RMI, including self (local call)
        for (int i=0; i < this.peers.length; ++i) {
            Response prepareResponse;
            
            // Proposal to self
            if (this.me == i) {
                prepareResponse = this.Prepare(prepareRequest);
            // Proposal to peers
            } else {
                prepareResponse = this.Call("Prepare", prepareRequest, i);
            }

            // We receive a response and the peer we sent to accepts the proposal
            if (null != prepareResponse && prepareResponse.proposalAccepted) {
                ++numAccepted;
                if(prepareResponse.proposalNum > n_a) {
                	n_a = prepareResponse.proposalNum;
                	vPrime = prepareResponse.value;
                }
            }
            
        }

        Response proposalResponse = new Response();
        
        if(numAccepted >= this.npaxos/2 + 1) {
        	proposalResponse.majorityAccepted = true;
        	proposalResponse.proposalNum = generatedProposalNum;			// used to send accept(n, v')
        	proposalResponse.value = vPrime;
        	
        }
        return proposalResponse;
    }
    

    public Response Prepare(Request req){
    	
        Response proposalResponse = new Response();
        Instance targetInstance = this.getInstance(req.seq);

        // Send the acceptance message if the received proposal number is greater than any this instance has seen
        if (req.proposalNum > targetInstance.highestProposal) {
            targetInstance.highestProposal = req.proposalNum;
            // @TODO: this here or during accept?
            targetInstance.value = targetInstance.value == null ? req.val : targetInstance.value;
            proposalResponse.seq = req.seq;
            proposalResponse.proposalNum = targetInstance.highestProposal;
            proposalResponse.value = targetInstance.value;
            proposalResponse.proposalAccepted = true;

        // Send rejection message otherwise
        } else {
            proposalResponse.proposalNum = targetInstance.highestProposal;
            proposalResponse.value = targetInstance.value;
            System.out.println("Paxos " + this.me + " rejecting proposal of " + req);
        }

    	return proposalResponse;
    }

    public boolean sendAcceptRequests(Request acceptRequest) {
    	Instance instance = this.getInstance(acceptRequest.seq);
    	int proposalNum = instance.highestProposal;
    	Object value = instance.value;
    	
    	int numAccepted = 0;
    	for(int i = 0; i < this.npaxos; ++i) {
    		Response acceptResponse;
    		if(i == this.me) {
    			acceptResponse = this.Accept(acceptRequest);
    		}
    		else {
    			acceptResponse = this.Call("Accept", acceptRequest, i);
    		}
    		
    		if(null != acceptResponse && acceptResponse.acceptAccepted) {
    			++numAccepted;
    		}
    	}
        return numAccepted >= this.npaxos/2 + 1;
    }

    public Response Accept(Request req) {
    	Response acceptorResponse;
    	Instance targetInstance = this.getInstance(seq);
    	
    	// from pseudo code (acceptor's accept handler)
    	// Similar to Prepare, except the condition below is >= instead of = and we update highest accept/proposal values
    	if(req.proposalNum >= targetInstance.highestProposal) {
    		targetInstance.highestProposal = req.proposalNum;
    		targetInstance.highestAccepted = req.proposalNum;
    		targetInstance.value = req.val;
    		acceptorResponse = new Response(
//    				req.proposalNum,
//    				targetInstance.highestAccepted,
//    				targetInstance.value
    		);
    		acceptorResponse.acceptAccepted = true;
    	}
    	else {
    		acceptorResponse = new Response();
    		acceptorResponse.acceptAccepted = false;
    		
    	}
    	return acceptorResponse;
    }

    
    // Need to look into this more!
    public void sendDecideRequests(Request decideRequest) {
        Instance instance = this.instances.get(decideRequest.seq);		// key should exist
        
        // needed for RMI call
        instance.highestProposal = decideRequest.proposalNum;
        instance.highestAccepted = decideRequest.proposalNum;
        instance.value = decideRequest.val;
        instance.state = State.Decided;
        //this.dones[this.me] = decideRequest.done;  //@TODO: needed??

        for(int i = 0; i < this.npaxos; ++i) {
        	Response decideResponse;
        	if(i != me) {
        		int done = this.dones[this.me];
        		decideResponse = this.Call(
        		        "Decide",
                        new Request(
                                decideRequest.seq,
                                decideRequest.proposalNum,
                                decideRequest.val,
                                done,
                                this.me
                        ),
                        i
                );
        	}
        }
    }

    public Response Decide(Request req){
        System.out.println("Decide for Paxos " + this.me + " with " + req);
        Instance instance = this.getInstance(req.seq);
        instance.highestProposal = req.proposalNum;
        instance.highestAccepted = req.proposalNum;
        instance.value = req.val;
        instance.state = State.Decided;
        System.out.println("Paxos " + this.me + " decided " + this.getInstance(req.seq) + "\n    seq: " + req.seq);

        //Done(req.seq);
        
        this.dones[req.me] = req.done;
        
    	return new Response();
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Any value <= seq should be forgotten!
        if(seq > this.dones[this.me])
            this.dones[this.me] = seq;
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Simply iterate through the keys to get the max seq number
    	int max = Integer.MIN_VALUE;
    	for(int key : instances.keySet())
    		if(key > max)
    			max = key;
    	return max;
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
        // retrieve min seq value
    	int min = this.dones[this.me];
    	
    	// search through all peer values to find the min seq from the dones array
    	for(int i : this.dones){
    		if(min > i)
    			min = i;
    	}
    	// all sequence values that are less than the min or finished deciding should be forgotten/deleted
    	for(int seq: instances.keySet()) {
    		if(seq < min || instances.get(seq).state == State.Decided) {
                //instances.remove(seq);
            }
    		if(seq > min) continue;
    		if(instances.get(seq).state != State.Decided) continue;
    		instances.remove(seq);
    	}
    	//just following the instructions above lol
    	return min + 1;
    }



    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Any sequence less than the Min value should've been removed and forgotten to save memory (from above)
    	if(seq < this.Min()) {
    	    System.out.println("Seq " + seq + " has been forgotten with min: " + this.Min());
            return new retStatus(State.Forgotten, null);
        }
    	
    	// Check if the sequence exists in the instances map
    	if(this.instances.containsKey(seq)) {
    		Instance targetInstance = instances.get(seq);
    		System.out.println("Paxos " + this.me + " status of seq " + seq + " " + targetInstance);
    		return new retStatus(targetInstance.state, targetInstance.value);
    	}
    	else {
    		// sequence number doesn't exist yet, so state is pending.
            System.out.println("Seq " + seq + " does not exist yet");
    		return new retStatus(State.Pending, null);
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