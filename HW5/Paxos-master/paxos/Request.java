package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=1L;
    // Your data here
    public int seq;
    public int proposalNum;
    public Object val;
    public int done;
    public int me;

    // Your constructor and methods here
    public Request() {
    	this.seq = -1;
    	this.proposalNum = Integer.MIN_VALUE;
    	this.val = null;
    }
    
    public Request(int seq, int proposalNum, Object val) {
    	this.seq = seq;
    	this.proposalNum = proposalNum;
    	this.val= val;
    }
    
    
    public Request(int seq, int proposalNum, Object val, int done, int me) {
    	this.seq = seq;
    	this.proposalNum = proposalNum;
    	this.val = val;
    	this.done = done;
    	this.me = me;
    }

    @Override
    public String toString() {
        String valStr;
        try {
            valStr = (String)this.val;
        } catch (ClassCastException e) {
            valStr = Integer.toString((int)this.val);
        }
        String str = (
                    "Request values:\n" +
                            "\tseq: " + this.seq + "\n" +
                            "\tproposalNum: " + this.proposalNum + "\n" +
                            "\tdone: " + this.done + "\n" +
                            "\tme: " + this.me + "\n" +
                            "\tval: " + valStr + "\n"
                );
        return str;
    }
}
