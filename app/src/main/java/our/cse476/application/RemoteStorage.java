package our.cse476.application;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class RemoteStorage extends AppCompatActivity {

    private boolean isDone = false;
    final Vector<Map<String, Object>> documents = new Vector<>();
    private String userCollName;

    public interface UploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    public interface DownloadCallback{
        void onSuccess(Map<String,Object> userData);
        void onFailure(Exception e);
    }

    public interface CollectionCallback{
        void onSuccess();
        void onFailure(Exception e);
    }
    public boolean uploadUser(Map<String, Object> input, UploadCallback callback) {
        if (input.isEmpty()){
            Log.d(TAG, "Input is empty");
            callback.onFailure(new IllegalArgumentException("Input is Invalid"));
            return false;
        }
        //check to make sure the input contains a username and password, if not, return false
        if (input.size() < 2){
            Log.d(TAG, "Input does not contain a username and password");
            callback.onFailure(new IllegalArgumentException("Input does not contain a username and password"));
            return false;
        }
        final boolean[] success = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Set the string documentName to the the value of the location mapped to "username" in the map
        String documentName = input.get("username").toString();
        userCollName = documentName+input.get("password").toString();
        if (documentName.equals("")){
            Log.d(TAG, "Document Name is empty");
            callback.onFailure(new IllegalArgumentException("Document Name is empty"));
            return false;
        }

        Map<String,Object> combo = new HashMap<>();
        db.collection(userCollName).document("NULL").set(combo);
        db.collection("users").document(documentName).set(input)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Upload User Successful, added with ID: " + documentName);
                        callback.onSuccess();
                        success[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document[UU]", e);
                        callback.onFailure(e);
                    }
                });
        return success[0];
    }
    public boolean uploadToCollection(String collection,Map<String, Object> input) {
        if (collection.isEmpty()){
            Log.d(TAG, "Collection Name is empty");
            return false;
        }
        if (input.isEmpty()){
            Log.d(TAG, "Input is empty");
            return false;
        }
        //check to make sure the input contains a username and password, if not, return false
        if (input.size() < 2){
            Log.d(TAG, "Input does not contain a username and password");
            return false;
        }
        final boolean[] success = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Set the string documentName to the the value of the location mapped to "username" in the map
        String documentName = input.get("username").toString();

        db.collection(collection).document(documentName).set(input)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Upload To Collection successful, added with ID: " + documentName);
                        success[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document[UTC]", e);
                    }
                });
        return success[0];
    }

    //downloadUser will look for a document where the title of the document is the username,
    // and will then see if the username in said document matches the username, and password matches the password
    public Map<String,Object> downloadUser(String username, String password ,DownloadCallback callback){
        final Map<String,Object>[] user = new Map[]{null};
        if (username.isEmpty() || password.isEmpty()){
            Log.d(TAG, "Username or Password is empty");
            callback.onFailure(new IllegalArgumentException("Username or Password is empty"));
            return null;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(username);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    user[0] = documentSnapshot.getData();
                    if (user[0].get("username").equals(username) && user[0].get("password").equals(password)){
                        Log.d(TAG, "Download User Successful: " + documentSnapshot.getData());
                        callback.onSuccess(user[0]);
                    }
                    else{
                        Log.d(TAG, "Username or Password is incorrect");
                        callback.onFailure(new IllegalArgumentException("Username or Password is incorrect"));
                    }
                } else {
                    Log.d(TAG, "No such document[DU]");
                    callback.onFailure(new IllegalArgumentException("No such document"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "get failed with ", e);
                callback.onFailure(e);
            }
        });
        return user[0];
    }
    public Map<String,Object> downloadDocumentFromName(String collection, String documentName){
        if (documentName.isEmpty()){
            Log.d(TAG, "Document Name is empty");
            return null;
        }
        if (collection.isEmpty()){
            Log.d(TAG, "Collection Name is empty");
            return null;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(collection).document(documentName);
        final Map<String,Object>[] document = new Map[]{null};
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    document[0] = documentSnapshot.getData();
                    Log.d(TAG, "Download Document From Name Successful: " + documentSnapshot.getData());
                } else {
                    Log.d(TAG, "No such document[DCFM]");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "get failed with ", e);
            }
        });
        return document[0];
        }

        //LogDatabase will log the entire database by taking in a collection and logging each document and it's data in the collection

    public void LogDatabase(String collection){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(collection).get().addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.QuerySnapshot>() {
                @Override
                public void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
                    Log.d(TAG, "---BEGINNING OF LOG---");
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Log.d(TAG, document.getId() +  "'s data: " + document.getData());
                    }
                    Log.d(TAG, "---END OF LOG---");
                }
            });
        }


    public void getCollection(String collection, CollectionCallback callback){
        if (collection.isEmpty()){
            Log.d(TAG, "Collection Name is empty");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(collection).get().addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    //Dont add the "Null" document
                    if (document.getId().equals("NULL")){
                        continue;
                    }
                    documents.add(document.getData());
                    Log.d(TAG, "Grabbed 1 file from collection: " + collection);
                }
                callback.onSuccess();
            }
        }
        ).addOnFailureListener(e -> {
            Log.d(TAG, "Error getting documents[GC]", e);
            callback.onFailure(e);
        });

    }
    public Vector<Map<String, Object>> loadCollection(){
        if(!documents.isEmpty()){
            return documents;
        }
        return null;
    }

    public void uploadTask(Map<String, Object> input,String owner) {
        if (input.isEmpty()){
            Log.d(TAG, "Input is empty");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Set the string documentName to the the value of the location mapped to "username" in the map
        String documentName = input.get("taskName").toString();
        if (documentName.equals("")){
            Log.d(TAG, "Document Name is empty");
        }
        Map<String,Object> combo = new HashMap<>();
        db.collection(owner).document(documentName).set(input)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Upload Task Successful, added with ID: " + documentName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document[UT]", e);
                    }
                });
    }

}
