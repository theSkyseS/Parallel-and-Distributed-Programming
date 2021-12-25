import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.BasicConfigurator;

import java.io.File;

public class Main extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new Main(), args);
        System.exit(exitCode);
    }

    public int run(String[] args) throws Exception {
        /*if (args.length != 2) {
            System.err.printf("Usage: %s [generic options] <input> <output>\n",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }*/

        FileUtils.deleteDirectory(new File("hadoopOut"));

        BasicConfigurator.configure();

        Job job = Job.getInstance();
        job.setJarByClass(Main.class);
        job.setJobName("Class Counter");
        FileInputFormat.addInputPath(job, new Path("spring-framework-main/spring-core/src"));
        FileOutputFormat.setOutputPath(job, new Path("hadoopOut"));

        FileInputFormat.setInputDirRecursive(job, true);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setMapperClass(ClassCountMapper.class);
        job.setReducerClass(ClassCountReducer.class);
        int returnValue = job.waitForCompletion(true) ? 0 : 1;
        System.out.println("job.isSuccessful " + job.isSuccessful());
        return returnValue;
    }
}