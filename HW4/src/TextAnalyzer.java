import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// Import everything for now...
import java.util.*;
import java.io.*;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

  /*---------------------------------------------------------------------------*/
  // TEXT MAPPER
    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>

  public static class TextMapper extends Mapper<LongWritable, Text, Text, Text> {
    public void map(LongWritable key, Text value, Context context)
    throws IOException, InterruptedException {
      // Implementation of your mapper function

      // Read line and format accordingly (lowercase, non-word character string)
      // replaceAll("[^\\p{L}\\p{Nd}]+", "") also works as well in case of UTF8 
      String line = value.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
      String[] words = line.trim().split("\\s+");
      Set<String> contextWords = new HashSet<String>();

      // ---------------------------------------------
      // new stuffs
      
      for(int i = 0; i < words.length; i++){
        if(!contextWords.contains(words[i])){
          // for each context word
          Text contextword = new Text(words[i]);
          ArrayList<String> list = new ArrayList<>();
          contextWords.add(words[i]);

          // Populate list with query words
          for(int j = 0; j < words.length; j++){
            if(i != j){
              context.write(contextword, new Text(words[j]));
            }
          }
        } 
      }
    }
  }

  /*---------------------------------------------------------------------------*/
  // TEXT COMBINER
    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper

  public static class TextCombiner extends Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterable<Text> wordLists, Context context)
    throws IOException, InterruptedException {

    TreeMap<String, Integer> map = new TreeMap<>();
    ArrayList<String> maxQueryWords = new ArrayList<String>();
    int highCount = 0;

    // Do quick maths on querywords for a specific contextword
    for (Text qWord : wordLists){
      // Populate TreeMap to contain all querywords and occurrences
      // if the set doesn't contain the queryword, add a new entry
      String queryWord = qWord.toString();
      if (!map.containsKey(queryWord)) {
        map.put(queryWord, 1);  
        if (1 > highCount) {
          highCount = 1;
        }
      }
      
      // update an entry that already exists!
      else {
        map.put(queryWord, map.get(queryWord)+1);
        highCount = highCount < map.get(queryWord) ? 
                                map.get(queryWord) : highCount;
      }
    }


      // Find the subset of querywords with value highCount
      for (String word : map.keySet()){
        if(map.get(word) == highCount){
          maxQueryWords.add(word);
        }
      }

      // send highCount
      // context.write(key, new Text(Integer.toString(highCount)));

      // send highCount tuples
      // for(String maxQWord : maxQueryWords){
      //   // remove highCount tuples from the map to get rid of dupes
      //   // map.remove(maxQWord);
      //   String output = maxQWord + " " + Integer.toString(highCount);
      //   context.write(key, new Text(output));
      // }

      // send queryWord tuples
      for(String word : map.keySet()){
        String output = word + " " + Integer.toString(map.get(word));
        context.write(key, new Text(output));
      }
    }
  }

  /*---------------------------------------------------------------------------*/
  // TEXT REDUCER
    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function

  public static class TextReducer extends Reducer<Text, Text, Text, Text> {
    private final static Text emptyText = new Text("");

    public void reduce(Text key, Iterable<Text> masterList, Context context)
    throws IOException, InterruptedException {
      // Implementation of your reducer function
      TreeMap<String, Integer> map = new TreeMap<>();
      ArrayList<Text> maxQueryWords = new ArrayList<Text>();
      int highCount = 0;

      // Parse tuples to get <queryword, occurrence>
      for (Text tuple : masterList){
        String[] strTuple = tuple.toString().split(" ");
        String queryWord = strTuple[0];
        int count = Integer.parseInt(strTuple[1]);

        if (!map.containsKey(queryWord)) {
          map.put(queryWord, count);  
          if (count > highCount) {
            highCount = count;
          }
        }
        
        // update an entry that already exists!
        else {
          map.put(queryWord, map.get(queryWord)+count);
          highCount = highCount < map.get(queryWord) ? 
                                  map.get(queryWord) : highCount;
        }
      }

      //Find subset of querywords with value highCount
      
      for (String word : map.keySet()){
        if(map.get(word) == highCount){
          maxQueryWords.add(new Text(word));
          // map.remove(word);
        }
      }

      if (!key.toString().equals("") && !key.toString().equals(" ")) {
        // Print out the context Word
        context.write(key, new Text(Integer.toString(highCount)));

        // Print out highCount querywords
        for (Text maxWord : maxQueryWords){
          String queryWordCount = Integer.toString(highCount) + ">";
          Text queryWordText = new Text(maxWord);
          queryWordText.set(("<" + maxWord + ",").trim());
          context.write(queryWordText, new Text(queryWordCount)); 
        }

        // Print out rest of the querywords
        for (String qWord : map.keySet()) {
          if (map.get(qWord) != highCount){
            Text queryWordText = new Text(qWord);
            String queryWordCount = map.get(qWord).toString().trim() + ">";
            queryWordText.set(("<" + qWord + ",").trim());
            context.write(queryWordText, new Text(queryWordCount));
          }
        }
        context.write(emptyText, emptyText);
      }
    }
  }

  /*---------------------------------------------------------------------------*/

  public int run(String[] args) throws Exception {
    Configuration conf = this.getConf();

    // Create job
    Job job = new Job(conf, "am74874_dpv292"); // Replace with your EIDs
    job.setJarByClass(TextAnalyzer.class);

    // Setup MapReduce job
    job.setMapperClass(TextMapper.class);
    //   Uncomment the following line if you want to use Combiner class
    job.setCombinerClass(TextCombiner.class);
    job.setReducerClass(TextReducer.class);

    // Specify key / value types (Don't change them for the purpose of this assignment)
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    //   If your mapper and combiner's  output types are different from Text.class,
    //   then uncomment the following lines to specify the data types.
    //job.setMapOutputKeyClass(?.class);
    //job.setMapOutputValueClass(TextArrayWritable.class);

    // Input
    FileInputFormat.addInputPath(job, new Path(args[0]));
    job.setInputFormatClass(TextInputFormat.class);

    // Output
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    job.setOutputFormatClass(TextOutputFormat.class);

    // Execute job and return status
    return job.waitForCompletion(true) ? 0 : 1;
  }

  // Do not modify the main method
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
    System.exit(res);
  }
}
