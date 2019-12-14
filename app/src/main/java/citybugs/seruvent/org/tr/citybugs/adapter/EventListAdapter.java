package citybugs.seruvent.org.tr.citybugs.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import citybugs.seruvent.org.tr.citybugs.R;
import citybugs.seruvent.org.tr.citybugs.model.Event;
import citybugs.seruvent.org.tr.citybugs.model.EventResources;
import citybugs.seruvent.org.tr.citybugs.util.Resource;

public class EventListAdapter extends ArrayAdapter {

    private Context context;
    private List<Event> eventList;

    public EventListAdapter(@NonNull Context context) {
        super(context, R.layout.adapter_event_list);
        this.context  = context;
        eventList = new ArrayList<Event>();
    }

    public EventListAdapter(@NonNull Context context , List<Event> eventList) {
        super(context, R.layout.adapter_event_list);
        this.context  = context;
        this.eventList = eventList;
    }

    public void addEventList(List<Event> eventList){
        this.eventList.addAll(eventList);
    }

    @Override
    public int getCount(){
        return eventList.size();
    }

    @Override
    public Object getItem(int position){
        return this.eventList.get(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewgroup){

        final ViewHolder holder;
        if(view==null ) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_event_list, viewgroup, false);
            holder = new ViewHolder();
            holder.eventImage = view.findViewById(R.id.activity_product_list_item_image);


            // Get event resource list
            if(eventList.get(position).getEventResourcesList()!=null && eventList.get(position).getEventResourcesList().size()>0){
                EventResources eventResource = eventList.get(position).getEventResourcesList().get(0);
                StorageReference fileRef = FirebaseStorage.getInstance().getReference();

                fileRef.child(eventResource.getUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Log.i(Resource.TAG_LOG_INFO, Resource.FIREBASE_DOMAIN_NAME + uri.getPath());

                        Glide.with(context).load(uri).into(holder.eventImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }

            holder.eventTitleTextView = view.findViewById(R.id.activity_product_list_item_product_name);
            holder.eventTitleTextView.setText(eventList.get(position).getTitle());

        }else{
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }

    /**
     * ViewHolder her defasında adapter update yapılmasını engellemek için kullanılmaktadır
     * Örneğin paging ile scroll yapılıyorsa ve yeni item eklenecekse bu gibi durumlarda kullanılır
     *      çünkü eski item'lar tutulmalı, yenileri ise listenin sonuna eklenmelidir
     *
     * Ancak notifyDataSetChanged ile sürekli ürün eklenip çıkarılıyorsa ViewHolder kullanılmasına
     *      gerek yoktur.
     */
    static class ViewHolder {
        ImageView eventImage;
        TextView eventTitleTextView;
    }

}
