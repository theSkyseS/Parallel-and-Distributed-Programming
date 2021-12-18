import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ClassCountReducer extends Reducer<Text, TextArrayWritable, Text, Text> {

    public final void reduce(final Text key, final Iterable<TextArrayWritable> values, final Context context) throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        for (TextArrayWritable val : values) {
            Writable[] writables = val.get();
            for (Writable writable : writables) {
                stringBuilder.append(writable);
                stringBuilder.append("  ");
            }
        }
        context.write(key, new Text(stringBuilder.toString()));
    }
}
