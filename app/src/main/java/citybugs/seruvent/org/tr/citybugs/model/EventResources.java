package citybugs.seruvent.org.tr.citybugs.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class EventResources {

    private long id;
    private String url;
    private Timestamp registerDate;
    private Timestamp updateDate;
    private Timestamp deleteDate;


    public EventResources(JSONObject jsonObject){
        try {
            this.id = jsonObject.has("id")?
                    jsonObject.getInt("id") : -1 ;
            this.url = jsonObject.has("url") && !jsonObject.isNull("url")?
                    jsonObject.getString("url"):"";
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<EventResources> EventResourcesList(JSONArray jsonArray){

        List<EventResources> eventResourcesList = new ArrayList<EventResources>();
        for(int i=0; i<jsonArray.length(); i++){
            try {
                eventResourcesList.add(new EventResources((JSONObject) jsonArray.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return eventResourcesList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}
