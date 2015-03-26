package nodomain.freeyourgadget.gadgetbridge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.adapter.GBDeviceAppAdapter;


public class AppManagerActivity extends Activity {
    public static final String ACTION_REFRESH_APPLIST
            = "nodomain.freeyourgadget.gadgetbride.appmanager.action.refresh_applist";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ControlCenter.ACTION_QUIT)) {
                finish();
            } else if (action.equals(ACTION_REFRESH_APPLIST)) {
                appList.clear();
                int appCount = intent.getIntExtra("app_count", 0);
                for (Integer i = 0; i < appCount; i++) {
                    String appName = intent.getStringExtra("app_name" + i.toString());
                    String appCreator = intent.getStringExtra("app_creator" + i.toString());
                    int id = intent.getIntExtra("app_id" + i.toString(), -1);
                    int index = intent.getIntExtra("app_index" + i.toString(), -1);

                    appList.add(new GBDeviceApp(id, index, appName, appCreator, ""));
                }
                mGBDeviceAppAdapter.notifyDataSetChanged();
            }
        }
    };
    final List<GBDeviceApp> appList = new ArrayList<>();
    private final String TAG = this.getClass().getSimpleName();
    private ListView appListView;
    private GBDeviceAppAdapter mGBDeviceAppAdapter;
    private GBDeviceApp selectedApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appmanager);

        appListView = (ListView) findViewById(R.id.appListView);
        mGBDeviceAppAdapter = new GBDeviceAppAdapter(this, appList);
        appListView.setAdapter(this.mGBDeviceAppAdapter);
        registerForContextMenu(appListView);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ControlCenter.ACTION_QUIT);
        filter.addAction(ACTION_REFRESH_APPLIST);
        registerReceiver(mReceiver, filter);

        Intent startIntent = new Intent(this, BluetoothCommunicationService.class);
        startIntent.setAction(BluetoothCommunicationService.ACTION_REQUEST_APPINFO);
        startService(startIntent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(
                R.menu.appmanager_context, menu);
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedApp = appList.get(acmi.position);
        menu.setHeaderTitle(selectedApp.getName());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appmanager_app_delete:
                if (selectedApp != null) {
                    Intent deleteIntent = new Intent(this, BluetoothCommunicationService.class);
                    deleteIntent.setAction(BluetoothCommunicationService.ACTION_DELETEAPP);
                    deleteIntent.putExtra("app_id", selectedApp.getId());
                    deleteIntent.putExtra("app_index", selectedApp.getIndex());
                    startService(deleteIntent);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}