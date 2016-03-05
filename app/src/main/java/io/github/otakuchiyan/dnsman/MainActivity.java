package io.github.otakuchiyan.dnsman;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity {
    private DnsStorage dnsStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dnsStorage = new DnsStorage(this);

        setTitle();
        firstBoot();

        setListView();
    }

    private void setTitle(){
        final PackageManager pm = getPackageManager();
        try{
            final PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            String label = "DNS man " + info.versionName;

            ActionBar actionBar = getActionBar();
            if(actionBar != null){
                actionBar.setTitle(label);
            }
        }catch (PackageManager.NameNotFoundException e){
            throw new AssertionError(e);
        }
    }

    //List part START
    private List<Map<String, String>> buildList(){

        List<Map<String, String>> dnsEntryList = new ArrayList<>();

        for(NetworkInfo info : DnsStorage.supportedNetInfoList){
            dnsEntryList.add(getNetworkDnsEntry(info));
        }
        dnsEntryList.add(getGlobalDnsEntry());
        return dnsEntryList;
    }

    private Map<String, String> getNetworkDnsEntry(NetworkInfo info){
        return getDnsEntry(info.getTypeName(), DnsStorage.info2resMap.get(info));
    }

    private Map<String, String> getGlobalDnsEntry(){
        return getDnsEntry("g", R.string.category_global);
    }

    private Map<String, String> getDnsEntry(String prefix, int resource){
        Map<String, String> dnsEntry = new HashMap<>();
        dnsEntry.put("prefix", prefix);
        dnsEntry.put("label", getText(resource).toString());

        String[] dnsData = dnsStorage.getDnsByKeyPrefix(prefix);
        String dnsEntryString = "";
        boolean isNoDns = false;

        if(dnsData[0].isEmpty() && dnsData[1].isEmpty()){
            dnsEntryString = getText(R.string.notify_no_dns).toString();
            isNoDns = true;
        }
        if(!isNoDns) {
            if (dnsData[0].isEmpty()) {
                dnsEntryString += dnsData[0] + '\t';
            }
            dnsEntryString += dnsData[1];
        }

        dnsEntry.put("dnsText", dnsEntryString);
        return dnsEntry;
    }

    private void setListView(){
        SimpleAdapter adapter = new SimpleAdapter(this, buildList(),
                android.R.layout.simple_list_item_2,
                new String[] {"label", "dnsText"},
                new int[] {android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> dnsEntry = (Map<String, String>) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), DnsEditActivity.class);
                i.putExtra("label", dnsEntry.get("label"));
                i.putExtra("prefix", dnsEntry.get("prefix"));
                startActivity(i);
            }
        });
    }
    //List part END

    private void firstBoot(){
        dnsStorage.initDnsMap(this);
    }
}



