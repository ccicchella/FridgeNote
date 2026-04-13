package our.cse476.application;

import android.graphics.Bitmap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Task implements Serializable {
    private String name;
    private int points;
    private boolean photoRequired;
    private transient Bitmap imageBitmap;
    private String urgency;
    private String id; // Unique identifier for the task

    private boolean isCompleted = false;

    public Task(String name, int points, boolean photoRequired, String urgency) {
        this.name = name;
        this.points = points;
        this.photoRequired = photoRequired;
        this.urgency = urgency;
        this.id = UUID.randomUUID().toString(); // Generate a unique ID for each Task
    }

    // Function that converts a Task object to a Map object
    public Map<String,Object> TasktoMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("taskName",name);
        map.put("taskPoints",points);
        map.put("taskPhotoRequired",photoRequired);
        map.put("taskUrgency",urgency);
        map.put("taskId",id);
        map.put("taskCompleted", isCompleted);
        map.put("taskImage", null);
        // Add the image to the map if it is not null
//        if (imageBitmap != null)
//        {
//            map.put("taskImage", imageBitmap);
//        }

        return map;
    }

    public void MapToTask(Map<String,Object> map){
        name = map.get("taskName").toString();
        points = Integer.parseInt(map.get("taskPoints").toString());
        photoRequired = (boolean) map.get("taskPhotoRequired");
        urgency = map.get("taskUrgency").toString();
        id = map.get("taskId").toString();
        isCompleted = (boolean) map.get("taskCompleted");
        imageBitmap = (Bitmap) map.get("taskImage");
    }
    public String getName() {
        return name;
    }

    //set name
    public void setName(String name) {this.name = name;}

    public int getPoints() {
        return points;
    }

    //set points
    public void setPoints(int points) {this.points = points;}

    public boolean getPhotoRequired() {return photoRequired;}

    //set photoRequired
    public void setPhotoRequired(boolean photoRequired) {this.photoRequired = photoRequired;}

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getUrgency() {
        return urgency;
    }

    //set urgency
    public void setUrgency(String urgency) {this.urgency = urgency;}

    public String getId() {
        return id;
    }

    //set id
    public void setId(String id) {this.id = id;}

    public void setCompleted() {
        isCompleted = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
