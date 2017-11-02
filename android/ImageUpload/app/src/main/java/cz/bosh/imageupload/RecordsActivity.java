package cz.bosh.imageupload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by honza on 31.10.17.
 */

public class RecordsActivity extends Activity {

    ListView mList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final List<Long> ids = ImageApplication.database.selectAll();

        setContentView(R.layout.records);
        mList = (ListView) findViewById(R.id.records_list);

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.records_item, R.id.records_item_text, ids);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Database.Record dr = ImageApplication.database.selectById(ids.get(i));
                Intent intent = new Intent(RecordsActivity.this, ImageActivity.class);
                intent.putExtra(ImageActivity.INTENT_EXTRA_RECORD, dr);
                startActivity(intent);

            }
        });
    }
}
