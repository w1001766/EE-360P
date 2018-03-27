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

  public static class TextMapper extends Mapper<LongWritable, Text, Text, TextArrayWritable> {
    public void map(LongWritable key, Text value, Context context)
    throws IOException, InterruptedException {
      // Implementation of your mapper function

      // Read line and format accordingly (lowercase, non-word character string)
      // replaceAll("[^\\p{L}\\p{Nd}]+", "") also works as well in case of UTF8 
      String line = value.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
      String[] words = line.trim().split("\\s+");
      Set<String> contextWords = new HashSet<String>();

      // New implementation
      for(int i = 0; i < words.length; i++){
        if(!contextWords.contains(words[i])){
          // for each context word
          Text contextword = new Text(words[i]);
          ArrayList<String> list = new ArrayList<>();
          contextWords.add(words[i]);

          // Populate list with query words
          for(int j = 0; j < words.length; j++){
            if(i != j){
              list.add(words[j]);
            }
          }

          // Create the ArrayWritable to be sent to the combiner
          //String[] castList = (String[])list.toArray();
          Object [] objList = list.toArray();
          String [] castList = Arrays.copyOf(objList, objList.length, String[].class);
          Text [] textList = new Text[objList.length];
          for (int x=0; x < objList.length; ++x) {
            textList[x] = new Text(castList[x]);
          }
          TextArrayWritable wordList = new TextArrayWritable(textList);

          // Send out contextword, querywordsList
          context.write(contextword, wordList);
        }
      }
    }
  }

  /*---------------------------------------------------------------------------*/
  // TEXT COMBINER
    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper

  public static class TextCombiner extends Reducer<Text, TextArrayWritable, Text, TextArrayWritable> {
    public void reduce(Text key, Iterable<TextArrayWritable> wordLists, Context context)
    throws IOException, InterruptedException {

      ArrayList<Writable> masterList = new ArrayList<>();
      for(TextArrayWritable list : wordLists){
        Writable[] writableList = list.get();
        ArrayList<Writable> querys = new ArrayList<Writable>(Arrays.asList(writableList));
        masterList.addAll(querys);
      }

      // Convert ArrayList to array, and set it in ArrayWritable
      Object [] objList = masterList.toArray();
      Writable [] castList = Arrays.copyOf(objList, objList.length, Writable[].class);
      Text [] textList = new Text[objList.length];
      for (int x=0; x < objList.length; ++x) {
        textList[x] = (Text)castList[x];
      }
      TextArrayWritable combinedList = new TextArrayWritable(textList);

      // Send out contextword, querywordsList
      context.write(key, combinedList);
    }
  }

  /*---------------------------------------------------------------------------*/
  // TEXT REDUCER
    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function

  public static class TextReducer extends Reducer<Text, TextArrayWritable, Text, Text> {
    private final static Text emptyText = new Text("");

    public void reduce(Text key, Iterable<TextArrayWritable> queryLists, Context context)
    throws IOException, InterruptedException {
      // Implementation of your reducer function
      TreeMap<String, Integer> map = new TreeMap<>();

      int highCount = 0;
      
      // Populate TreeMap to contain all querywords and occurrences
      for (TextArrayWritable queryWords : queryLists) {
        for (Writable writeWord : queryWords.get()) {
          Text queryWord = (Text)writeWord;

          if (!map.containsKey((queryWord).toString())) {
            map.put((queryWord).toString(), 1);
            
            if (1 > highCount) {
              highCount = 1;
            }


          } else {
            map.put((queryWord).toString(), map.get((queryWord).toString())+1);

            highCount = highCount < map.get((queryWord).toString()) ? 
                                    map.get((queryWord).toString()) : highCount;
                                                                    
          }
        } 
      }

      // Find the subset of querywords with value highCount
      ArrayList<Text> maxQueryWords = new ArrayList<Text>();
      for (String word : map){
        if(map.get(word) == highCount){
          maxQueryWords.add(new Text(word));
          map.remove(word);
        }
      }
      
      if (!key.toString().equals("") && !key.toString().equals(" ")) {
        context.write(key, new Text(Integer.toString(highCount)));
        
        // Print out highCount querywords
        for (Text maxWord : maxQueryWords){
          String queryWordCount = highCount.toString() + ">";
          queryWordText.set(("<" + queryWord + ",").trim());
          context.write(queryWordText, new Text(queryWordCount)); 
        }

        // Print out rest of the querywords
        for (String queryWord : map.keySet()) {
          Text queryWordText = new Text(queryWord);
          String queryWordCount = map.get(queryWord).toString().trim() + ">";
          queryWordText.set(("<" + queryWord + ",").trim());
          context.write(queryWordText, new Text(queryWordCount));
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
    job.setMapOutputValueClass(TextArrayWritable.class);

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
  private static class TextArrayWritable extends ArrayWritable {
    public TextArrayWritable() {
      super(Text.class);
    }

    public TextArrayWritable(Text[] strings) {
      super(Text.class, strings);
    }
  }
}

// You may define sub-classes here.
