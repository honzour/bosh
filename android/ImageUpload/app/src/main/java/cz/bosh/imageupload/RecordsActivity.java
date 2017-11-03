package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by honza on 31.10.17.
 */

public class RecordsActivity extends Activity {

    private ListView mList;
    private List<Database.ShortRecord> ids;
    private List<String> captions;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.records);
        mList = (ListView) findViewById(R.id.records_list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Database.ShortRecord sr = ids.get(i);
                Database.Record dr = ImageApplication.database.selectById(sr.id);
                Intent intent = new Intent(RecordsActivity.this, ImageActivity.class);
                intent.putExtra(ImageActivity.INTENT_EXTRA_RECORD, dr);
                intent.putExtra(ImageActivity.INTENT_EXTRA_ID, sr);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ids = ImageApplication.database.selectAll();
        captions = new ArrayList<String>(ids.size());

        List<String> csv = ImageApplication.getCsv();
        Map<Integer, String> m = new HashMap<Integer, String>(csv.size());
        for (String line : csv) {
            try {
                String[] fields = line.split("\\$");
                line = fields[0] + ',' + fields[1] + ',' + fields[2];
                m.put(Integer.valueOf(fields[3]), line);
            } catch (Exception e) {
                continue;
                // TODO
            }

        }

        for (Database.ShortRecord r : ids) {
            captions.add(r.id + " " + m.get((int)r.shop));
        }
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.records_item, R.id.records_item_text, captions);
        mList.setAdapter(adapter);
    }
}
