package kvpaxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=11L;
    // Your data here
    String key;
    Integer value;
    Integer seq;

    // Your constructor and methods here
    Request(String k, Integer v, Integer s){
    	this.key = k;
    	this.value = v;
    	this.seq = s;
    }

}
