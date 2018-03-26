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

	public static class TextMapper extends Mapper<LongWritable, Text, Text, MapWritable> {
		public void map(LongWritable key, Text value, Context context)
		throws IOException, InterruptedException
		{
        // Implementation of your mapper function

        // Read line and format accordingly (lowercase, non-word character string)
        // replaceAll("[^\\p{L}\\p{Nd}]+", "") also works as well in case of UTF8 
			String line = value.toString().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
			String[] words = line.split(" ");

        // Create a set of all words and # of occurrences in line 
			TreeMap<String, IntWritable> wordSet = new TreeMap<>();
			for(String word: words){
				//Text word_txt = new Text(word);
				if(!wordSet.containsKey(word))
					wordSet.put(word, new IntWritable(1));
				else{
					IntWritable count = wordSet.get(word);
					int count_val = count.get() + 1;
					wordSet.put(word, new IntWritable(count_val));
				}
			}

      // Send key-value pairs <Text, MapWritable<Text, IntWritable>> to Combiner/Reducer
			for(String word : wordSet.keySet()){
				Text contextword = new Text(word);

        // Create MapWritables containing key-value pairs <queryword, occurrence>
				MapWritable wordmap = new MapWritable();

        // Remove a single occurence of that word from the wordset
        int count = wordSet.get(word).get()-1;
        if(count != 0){
					IntWritable count = wordSet.get(word);
					int count_val = count.get() - 1;
					wordSet.put(word, new IntWritable(count_val));
				}
				else
					wordSet.remove(word);

        // Write to MapWritable
				for(Map.Entry<String, IntWritable> entry: wordSet.entrySet())
					wordmap.put(new Text(entry.getKey()), entry.getValue());

        // Send out contextword, querywords<occurrences>
				context.write(contextword, wordmap);

        // Add the occurence back into the wordset
        if(count != 0){
					IntWritable count = wordSet.get(word);
					int count_val = count.get() + 1;
					wordSet.put(word, new IntWritable(count_val));
				}
				else
					wordSet.put(word, new IntWritable(1));
			}
		}
	}

	/*---------------------------------------------------------------------------*/
	// TEXT COMBINER
    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper

	public static class TextCombiner extends Reducer<Text, MapWritable, Text, MapWritable> {
		public void reduce(Text key, Iterable<MapWritable> wordmaps, Context context)
		throws IOException, InterruptedException
		{
      // Implementation of your combiner function
			TreeMap<String, IntWritable> wordSet = new TreeMap<>();

      // Combine querywords-occurrence pairs into one TreeMap
      for(MapWritable wordmap : wordmaps){
      	for(Map.Entry<Text, IntWritable> entry: wordmap.entrySet()){
					String query = entry.getKey().toString();
					IntWritable count = entry.getValue();
					if(wordSet.containsKey(query)){
						IntWritable value = wordSet.get(query);
						value.set(value.get() + count.get());
					}
					else{
						wordSet.put(query, count);
					}
      	}
			}

			// Create new MapWritable to send to the reducer
			MapWritable wordmap = new MapWritable();

			// Write to MapWritable
			for(Map.Entry<String, IntWritable> entry: wordSet.entrySet())
				wordmap.put(new Text(entry.getKey()), entry.getValue());

			// Send it off to the reducer to be outputted
			context.write(key, wordmap);
		}
	}

	/*---------------------------------------------------------------------------*/
	// TEXT REDUCER
    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function

	public static class TextReducer extends Reducer<Text, MapWritable, Text, Text> {
		private final static Text emptyText = new Text("");

		public void reduce(Text key, Iterable<MapWritable> wordmaps, Context context)
		throws IOException, InterruptedException
		{
      // Implementation of your reducer function
      TreeMap<String, IntWritable> map = new TreeMap<>();

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
        // job.setCombinerClass(TextCombiner.class);
        job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        //job.setMapOutputKeyClass(?.class);
        //job.setMapOutputValueClass(?.class);

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


