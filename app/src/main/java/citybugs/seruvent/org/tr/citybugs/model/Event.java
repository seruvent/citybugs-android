package citybugs.seruvent.org.tr.citybugs.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class Event {

    private long id;
    private String title;
    private String description;
    private String gpsLocation;
    private long state;
    private java.sql.Timestamp registerDate;
    private java.sql.Timestamp updateDate;
    private java.sql.Timestamp deleteDate;
    private List<EventResources> eventResourcesList=null;
    //private User user;


    public Event(JSONObject jsonObject){
        try {
            this.id = jsonObject.has("id")?
                    jsonObject.getInt("id") : -1 ;
            this.title = jsonObject.has("title") && !jsonObject.isNull("title")?
                    jsonObject.getString("title"):"";
            this.description = jsonObject.has("description") && !jsonObject.isNull("description")?
                    jsonObject.getString("description"):"-";

            if(jsonObject.has("eventResources") && !jsonObject.isNull("eventResources")){
                this.eventResourcesList = EventResources.EventResourcesList(jsonObject.getJSONArray("eventResources"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<Event> EventList(JSONArray jsonArray){

        List<Event> eventList = new ArrayList<Event>();
        for(int i=0; i<jsonArray.length(); i++){
            try {
                eventList.add(new Event((JSONObject) jsonArray.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return eventList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }

    public Timestamp getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Timestamp registerDate) {
        this.registerDate = registerDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    public Timestamp getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Timestamp deleteDate) {
        this.deleteDate = deleteDate;
    }

    public List<EventResources> getEventResourcesList() {
        return eventResourcesList;
    }

    public void setEventResourcesList(List<EventResources> eventResourcesList) {
        this.eventResourcesList = eventResourcesList;
    }
}
