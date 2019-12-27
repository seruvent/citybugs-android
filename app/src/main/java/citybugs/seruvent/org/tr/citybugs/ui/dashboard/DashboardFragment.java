package citybugs.seruvent.org.tr.citybugs.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import citybugs.seruvent.org.tr.citybugs.R;
import citybugs.seruvent.org.tr.citybugs.adapter.EventListAdapter;
import citybugs.seruvent.org.tr.citybugs.model.Event;
import citybugs.seruvent.org.tr.citybugs.util.Resource;
import citybugs.seruvent.org.tr.citybugs.util.Result;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private EventListAdapter eventListAdapter;
    private RequestQueue requestQueue;
    private JsonRequest jsonRequest;
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = view.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        // Initialization
        requestQueue = Volley.newRequestQueue(getContext());
        eventListAdapter = new EventListAdapter(getContext());


        getRecentEvents();

        return view;
    }


    public void getRecentEvents(){

        jsonRequest = new JsonObjectRequest(Request.Method.GET, Resource.DOMAIN_API_EVENT_LIST,null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try{

                    if(Result.SUCCESS.checkResult(new Result(response))){
                        JSONArray contentArray = response.getJSONArray("content");
                        if(contentArray.length()>0){

                            eventListAdapter.addEventList(Event.EventList(contentArray));
                            ListView listView = view.findViewById(R.id.fragment_event_list_view_main);
                            Log.i(Resource.TAG_LOG_INFO , "-- -- --");
                            if(listView.getAdapter()==null)
                                listView.setAdapter(eventListAdapter);
                            else{
                                eventListAdapter.notifyDataSetChanged();
                            }
                        }

                    }else if(Result.FAILURE_TOKEN.checkResult(new Result(response))){
                        //Resource.setDefaultAPITOKEN();
                        //Intent intent = new Intent(getApplicationContext() , LoginActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //startActivity(intent);
                    } else {
                        //Toast.makeText(getApplicationContext(), getString(R.string.unexpected_case_error) , Toast.LENGTH_LONG).show();
                        //Crashlytics.log(Log.INFO , Result.LOG_TAG_INFO.getResultText() , this.getClass().getName() + " >> " + Resource.ajax_get_product_by_search_key + " >> responseString = " + responseString);
                    }

                } catch (Exception e){
                    Log.e(Resource.TAG_LOG_ERROR, Resource.DOMAIN_API_EVENT_LIST + " - JSON exception - " + e.getMessage());
                    e.printStackTrace();
                } finally {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Resource.TAG_LOG_ERROR, error.toString());
                //showProgress(false);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("AuthXXX", "Bearer YYY");
                return params;
            }
        };

        jsonRequest.setTag(this.getClass().getName());
        requestQueue.add(jsonRequest);
    }
}