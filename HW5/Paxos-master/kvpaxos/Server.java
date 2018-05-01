package kvpaxos;
import paxos.Paxos;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

public class Server implements KVPaxosRMI {

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;
    int seqLast;
    
    String[] servers;
    int[] ports;
    KVPaxosRMI stub;

    // Your definitions here
    Map<String, Integer> KVStore;
    Map<Integer, Boolean> seqs;
    List<Op> log;
    
    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        // Your initialization code here

        KVStore = new ConcurrentHashMap<>();
        seqs = new ConcurrentHashMap<>();
        log = new ArrayList<>();
        seqLast = 1;

        
        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    public Op wait(int seq){
    	int t = 10;
	    while (true){
	    Paxos.retStatus ret = this.px.Status(seq);
		    if(ret.state == State.Decided)
		    	return Op.class.cast(ret.v);
		    
		    try{
		    Thread.sleep (t);
		    }
		    catch (Exception e){
		    	e.printStackTrace();
		    }
		    
		    if(t < 1000){
		    	t = t * 2;
		    }
	    }
    }
    
    
    // RMI handlers
    public Response Get(Request req){
        // Your code here
    	mutex.lock();
    	Op op = new Op("Get", req.seq, req.key, null);
    	
        boolean ok = false;
        Op log;
        while(!ok){
            int seq = this.seqLast;

            Paxos.retStatus ret = this.px.Status(this.seqLast);
            if(ret.state == State.Decided){
                log = Op.class.cast(ret.v);
            }
            else{
                this.px.Start(seq, op);
                log = this.wait(seq);

            }
            ok = (op.ClientSeq == log.ClientSeq);
            if(log.op != "Get"){
                this.log.add(log);
                this.KVStore.put(log.key, log.value);
            }

            this.seqs.put(log.ClientSeq, true);
            this.px.Done(this.seqLast);
            this.seqLast++;
        }
    	
    	mutex.unlock();
        return null;
    }

    public Response Put(Request req){
    	mutex.lock();
    	Response response = new Response();
    	if(seqs.containsKey(req.seq)) {
    		response.ack = true;
    		mutex.unlock();
    		return response;
    	}
    	else {
    		Op op = new Op("Put", req.seq, req.key, req.value);
    		
            boolean ok = false;
            Op log;
            while(!ok){
                int seq = this.seqLast;

                Paxos.retStatus ret = this.px.Status(this.seqLast);
                if(ret.state == State.Decided){
                    log = Op.class.cast(ret.v);
                }
                else{
                    this.px.Start(seq, op);
                    log = this.wait(seq);

                }
                ok = (op.ClientSeq == log.ClientSeq);
                if(log.op != "Get"){
                    this.log.add(log);
                    this.KVStore.put(log.key, log.value);
                }

                this.seqs.put(log.ClientSeq, true);
                this.px.Done(this.seqLast);
                this.seqLast++;
            }
    		
        	mutex.unlock();
            return response;
    	}
    	
    }


}
