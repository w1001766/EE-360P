package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    public int seq;
    public boolean ack;
    public int num;
    public Object value;


    // Your constructor and methods here
    public Response() {
    	this.seq = -1;
    	this.num = Integer.MIN_VALUE;
    	this.value = null;
    	this.ack = false;
    }
    
    public Response(int seq, int num, Object val) {
    	this.seq = seq;
    	this.num = num;
    	this.value = val;
    }
    
}
