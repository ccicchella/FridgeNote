package our.cse476.application;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.cardview.widget.CardView;

import our.cse476.application.databinding.ActivityDashboardBinding;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import android.widget.Button;
import android.graphics.Bitmap;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import static android.Manifest.permission.CAMERA;


public class DashboardActivity extends AppCompatActivity {

    // Binding object for the activity_dashboard layout
    private ActivityDashboardBinding binding;

    // List to hold tasks
    private ArrayList<Task> tasks = new ArrayList<>();

    //completed tasks
    private final ArrayList<Task> completedTasks = new ArrayList<>();

    // Request code for starting AddTaskActivity
    private static final int REQUEST_CODE_ADD_TASK = 1;

    // Request code for starting EditTask
    private static final int REQUEST_CODE_EDIT_TASK = 2;

    // Key for saving and restoring the task list state
    private static final String TASK_LIST_KEY = "task_list";

    //code for image capture
    private static final int REQUEST_IMAGE_CAPTURE = 22;

    //code for image request
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    //current task
    private Task currentTask;

    // current image view
    private ImageView currentImageView;

    // show completed tasks?
    private boolean showingCompletedTasks = false;

    private boolean camera = true;

    private String username;

    private String password;
    RemoteStorage remoteStorage = new RemoteStorage();




// DashboardActivity.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore the task list if savedInstanceState contains it
        if (savedInstanceState != null && savedInstanceState.containsKey(TASK_LIST_KEY)) {
            tasks = (ArrayList<Task>) savedInstanceState.getSerializable(TASK_LIST_KEY);
        }
        // Get the username from the Intent that started this activity
        username = getIntent().getStringExtra("EXTRA_USERNAME");
        password = getIntent().getStringExtra("EXTRA_PASSWORD");
        String collection = username + password;
        // Inflate the layout using ViewBinding
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.usernameTextView.setText("Welcome, " + username);

        // Set an OnClickListener on the task button to start AddTaskActivity
        binding.taskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, AddTaskActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
            }
        });

        // Set up the switch
        Switch taskSwitch = findViewById(R.id.history_switch);
        taskSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showingCompletedTasks = isChecked;
            displayTasks();
        });

        // Request camera permissions if not granted
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            if (!hasCamera()) {
                Toast.makeText(this, "No camera found on this device", Toast.LENGTH_SHORT).show();
                camera = false;
            }
        }

        remoteStorage.getCollection(collection, new RemoteStorage.CollectionCallback() {
            @Override
            public void onSuccess() {
                displayTasks();
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("ContentValues", "Failed to load collection: " + e.getMessage());
            }
        });

        // Display tasks in the task container after setting the layout
        displayTasks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!hasCamera()) {
                    Toast.makeText(this, "No camera found on this device", Toast.LENGTH_SHORT).show();
                    camera = false;
                }
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                camera = false;
            }
        }
    }

    private boolean hasCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        return numberOfCameras > 0;
    }


    public void addTask(Task task) {
        tasks.add(task);
        displayTasks();
    }
    // Method to display all tasks in the task container LinearLayout
    private void displayTasks() {
        // create container for task
        LinearLayout taskContainer = findViewById(R.id.taskContainer);
        taskContainer.removeAllViews();
        //determine which tasks to show
        ArrayList<Task> tasksToShow = tasks;
        username = getIntent().getStringExtra("EXTRA_USERNAME");
        password = getIntent().getStringExtra("EXTRA_PASSWORD");
        String collection = username+password;
        Vector<Map<String,Object>> incoming = remoteStorage.loadCollection();
        if (incoming != null) {
            for (Map<String, Object> taskMap : incoming) {
                Task task = TaskfromMap(taskMap);
                task.MapToTask(taskMap);
                boolean found = false;
                for (Task t : tasksToShow) {
                    if (t.getId().equals(task.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tasksToShow.add(task);
                }
            }
        }
        for (final Task task : tasksToShow) {
            if(showingCompletedTasks && task.isCompleted()){
                LinearLayout taskLayout = createTaskLayout(task);
                taskContainer.addView(taskLayout);
            }
            else if(!showingCompletedTasks && !task.isCompleted()){
                LinearLayout taskLayout = createTaskLayout(task);
                taskContainer.addView(taskLayout);
            }
            Map<String,Object> taskMap = task.TasktoMap();
            remoteStorage.uploadTask(taskMap,username+password);
        }
    }

    // function to create a task layout
    private LinearLayout createTaskLayout(final Task task) {
        LinearLayout taskLayout = new LinearLayout(this);
        taskLayout.setOrientation(LinearLayout.VERTICAL);
        taskLayout.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));

        // Creating a CardView to add elevation effect
        CardView cardView = new CardView(this);
        cardView.setCardElevation(dpToPx(4));
        cardView.setContentPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Set background based on urgency
        String urgency = task.getUrgency();
        if (urgency.equals("Low")) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.pastel_green));
        } else if (urgency.equals("Medium")) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.pastel_yellow));
        } else {
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.pastel_red));
        }

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Left part (2/3) of the card
        LinearLayout leftPart = new LinearLayout(this);
        leftPart.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftPartParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        leftPart.setLayoutParams(leftPartParams);

        TextView taskNameTextView = new TextView(this);
        taskNameTextView.setText(task.getName());
        taskNameTextView.setTextSize(16);
        taskNameTextView.setTypeface(null, Typeface.BOLD);
        taskNameTextView.setTextColor(ContextCompat.getColor(this, R.color.black));

        TextView taskPointsTextView = new TextView(this);
        taskPointsTextView.setText(task.getPoints() + " points");
        taskPointsTextView.setTextSize(14);
        taskPointsTextView.setTextColor(ContextCompat.getColor(this, R.color.black));

        leftPart.addView(taskNameTextView);
        leftPart.addView(taskPointsTextView);

        // Right part (1/3) of the card for the Done button or image view
        LinearLayout rightPart = new LinearLayout(this);
        rightPart.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightPartParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        rightPart.setLayoutParams(rightPartParams);

        Button doneButton = new Button(this);
        doneButton.setText("Done");

        if (!showingCompletedTasks) {
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.getPhotoRequired() && camera) {
                        currentTask = task;
                        currentImageView = null;
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else if (task.getPhotoRequired() && !camera) {
                        Toast.makeText(DashboardActivity.this, "Please enable the camera to complete this task", Toast.LENGTH_SHORT).show();
                    } else {
                        completedTasks.add(task);
                        task.setCompleted();
                        //tasks.remove(task);
                        displayTasks();
                    }
                }
            });
            rightPart.addView(doneButton);

            // Add the edit button only if the task is not completed
            Button editButton = new Button(this);
            editButton.setText("Edit");
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(DashboardActivity.this, AddTaskActivity.class);
                    editIntent.putExtra("EXTRA_TASK_TO_EDIT", task);
                    startActivityForResult(editIntent, REQUEST_CODE_EDIT_TASK);
                }
            });
            rightPart.addView(editButton);
        }

        innerLayout.addView(leftPart);
        innerLayout.addView(rightPart);

        // If the task is completed, replace the Done button with the image (if available)
        if (showingCompletedTasks && task.getImageBitmap() != null) {
            ImageView taskImageView = new ImageView(this);
            taskImageView.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(90), dpToPx(90)));
            taskImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            taskImageView.setImageBitmap(task.getImageBitmap());
            rightPart.removeAllViews(); // Remove the Done button
            rightPart.addView(taskImageView);
        }

        cardView.addView(innerLayout);
        taskLayout.addView(cardView);

        return taskLayout;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK && data != null) {
            // Retrieve the new task from the Intent
            Task newTask = (Task) data.getSerializableExtra("EXTRA_TASK");
            if (newTask != null) {
                // Add the new task to the task list and update the display
                tasks.add(newTask);
                displayTasks();
            }

            // If going to edit, go to task
        } else if (requestCode == REQUEST_CODE_EDIT_TASK && resultCode == RESULT_OK && data != null) {
            // Retrieve the edited task from the Intent
            Task editedTask = (Task) data.getSerializableExtra("EXTRA_TASK");
            if (editedTask != null) {
                // Find and update the task in the task list
                for (int i = 0; i < tasks.size(); i++) {
                    if (tasks.get(i).getId().equals(editedTask.getId())) { // Ensure you have a unique identifier method like getId()
                        tasks.set(i, editedTask); // Update the task at the found index
                        break;
                    }
                }
                displayTasks(); // Refresh the task list display
            }

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && camera) {
            // Check if the result comes from the camera and if it was successful
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            saveImage(imageBitmap);
            if (currentTask != null) {
                currentTask.setImageBitmap(imageBitmap);
                completedTasks.add(currentTask);
                currentTask.setCompleted();
                //tasks.remove(currentTask);
                //currentTask = null;
            }
            displayTasks();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && !camera) {
            Toast.makeText(this, "Camera is necessary, please enable permissions", Toast.LENGTH_SHORT).show();
        }
    }

    // Your saveImage method implementation
    private void saveImage(Bitmap finalBitmap) {

    }

    // Utility method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Function to create a Task object from a map
    public Task TaskfromMap(Map<String,Object> map) {
        Task task = new Task(map.get("taskName").toString(),
                Integer.parseInt(map.get("taskPoints").toString()),
                (boolean) map.get("taskPhotoRequired"),
                map.get("taskUrgency").toString());
        task.setId(map.get("taskId").toString());
        // Add the image to the task if it is not null
        if (map.containsKey("taskImage"))
        {
            task.setImageBitmap((Bitmap) map.get("taskImage"));
        }

        return task;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the task list to the outState bundle
        outState.putSerializable(TASK_LIST_KEY, tasks);
    }
}