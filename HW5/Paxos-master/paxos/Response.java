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
    public boolean proposalAccepted = false;
    public boolean acceptAccepted = false;
    public boolean majorityAccepted = false;
    public Object value;
    public int proposalNum = Integer.MIN_VALUE;


    // Your constructor and methods here
    public Response() {
    	this.seq = -2;
    	this.proposalNum = Integer.MIN_VALUE;
    	this.value = null;
    	this.proposalAccepted = false;
    	this.acceptAccepted = false;
    }
    
    public Response(int seq, int num, Object val) {
    	this.seq = seq;
    	this.proposalNum = num;
    	this.value = val;
    }

    @Override
    public String toString() {
        String valStr;
        try {
            valStr = (String)this.value;
        } catch (ClassCastException e) {
            valStr = Integer.toString((int)this.value);
        }
        String str = (
                "Response:\n" +
                        "\tseq: " + this.seq + "\n" +
                        "\tproposalAccepted: " + this.proposalAccepted + "\n" +
                        "\tacceptAccepted: " + this.acceptAccepted + "\n" +
                        "\tmajorityAccepted: " + this.majorityAccepted + "\n" +
                        "\tvalue: " + valStr + "\n" +
                        "\tproposalNum: " + this.proposalNum + "\n"
        );
        return str;
    }
}
