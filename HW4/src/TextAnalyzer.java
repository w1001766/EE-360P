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

  public static class TextMapper extends Mapper<LongWritable, Text, Text, ArrayWritable> {
    public void map(LongWritable key, Text value, Context context)
    throws IOException, InterruptedException {
      // Implementation of your mapper function

      // Read line and format accordingly (lowercase, non-word character string)
      // replaceAll("[^\\p{L}\\p{Nd}]+", "") also works as well in case of UTF8 
      String line = value.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
      String[] words = line.split(" ");

      // New implementation
      for(int i = 0; i < words.length; i++){
        // for each context word
        Text contextword = new Text(words[i]);
        ArrayList<String> list = new ArrayList<>();


        // Populate list with query words
        for(int j = 0; j < words.length; j++){
          if(i != j){
            list.add(words[j]);
          }
        }

        // Create the ArrayWritable to be sent to the combiner
        String[] castList = (String [])list.toArray();
        ArrayWritable wordList = new ArrayWritable(castList);

        // Send out contextword, querywordsList
        context.write(contextword, wordList);
      }
    }
  }

  /*---------------------------------------------------------------------------*/
  // TEXT COMBINER
    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper

  public static class TextCombiner extends Reducer<Text, ArrayWritable, Text, ArrayWritable> {
    public void reduce(Text key, Iterable<ArrayWritable> wordLists, Context context)
    throws IOException, InterruptedException {

      ArrayList<Writable> masterList = new ArrayList<>();
      for(ArrayWritable list : wordLists){
        ArrayList<Writable> querys = new ArrayList<>();
        masterList.addAll(querys);
      }

      // Convert ArrayList to array, and set it in ArrayWritable
      String[] mlWritable = (String[])masterList.toArray();
      ArrayWritable combinedList = new ArrayWritable(mlWritable);

      // Send out contextword, querywordsList
      context.write(key, combinedList);
    }
  }

  /*---------------------------------------------------------------------------*/
  // TEXT REDUCER
    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function

  public static class TextReducer extends Reducer<Text, ArrayWritable, Text, Text> {
    private final static Text emptyText = new Text("");

    public void reduce(Text key, Iterable<ArrayWritable> queryWords, Context context)
    throws IOException, InterruptedException {
      // Implementation of your reducer function
      TreeMap<String, Integer> map = new TreeMap<>();

      int highCount = 0;

      for (Writable queryWord : queryWords) {
        if (!key.toString().equals(queryWord.toString())) {

          if (!map.containsKey(queryWord.toString())) {
            map.put(queryWord.toString(), 1);
            
            if (1 > highCount) {
              highCount = 1;
            }


          } else {
            map.put(queryWord.toString(), map.get(queryWord.toString())+1);

            highCount = highCount < map.get(queryWord.toString()) ? 
                                    map.get(queryWord.toString()) : highCount;
                                                                    
          }
        }
      }

      context.write(key, new Text(Integer.toString(highCount)));

      for (String queryWord : map.keySet()) {
        Text queryWordText = new Text(queryWord);
        String queryWordCount = map.get(queryWord).toString() + ">";
        queryWordText.set("<" + queryWord + ",");
        context.write(queryWordText, new Text(queryWordCount));
      }

      context.write(emptyText, emptyText);

/*
      for(MapWritable wordmap : wordmaps){
        for(Map.Entry<Writable, Writable> entry: wordmap.entrySet()){
          Text queryText = (Text)entry.getKey();
          String query = queryText.toString();
          IntWritable count = (IntWritable)entry.getValue();
          map.put(query, count);
        }
      }

      // Write out the results; you may change the following example
      // code to fit with your reducer function.
      // Write out the current context key
      context.write(key, emptyText);

      // Write out query words and their count
      for(String queryWord: map.keySet()){
        Text queryWordText = new Text(queryWord);
        String count = map.get(queryWord).toString() + ">";
        queryWordText.set("<" + queryWord + ",");
        context.write(queryWordText, new Text(count));
      }

      // Empty line for ending the current context key
      context.write(emptyText, emptyText);
*/
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
    job.setMapOutputValueClass(MapWritable.class);

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

      /*---------------------------------------------------------------------------*/
}

// You may define sub-classes here.


