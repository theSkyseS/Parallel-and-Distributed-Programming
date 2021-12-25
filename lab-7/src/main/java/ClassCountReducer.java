import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ClassCountReducer extends Reducer<Text, Text, Text, Text> {

    public final void reduce(final Text key, final Iterable<Text> values, final Context context) throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Text val : values) {
            stringBuilder.append(val);
            stringBuilder.append("  ");
        }
        context.write(key, new Text(stringBuilder.toString()));
    }
}
