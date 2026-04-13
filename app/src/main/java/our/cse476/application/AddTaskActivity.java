package our.cse476.application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class AddTaskActivity extends AppCompatActivity {

    private EditText taskNameEditText;
    private EditText taskPointsEditText;
    private Button saveTaskButton;
    private Switch photoRequiredSwitch;
    private RadioGroup urgencyRadioGroup;
    private Task taskToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskNameEditText = findViewById(R.id.task_name);
        taskPointsEditText = findViewById(R.id.task_points);
        saveTaskButton = findViewById(R.id.save_task_button);
        photoRequiredSwitch = findViewById(R.id.photo_required);
        urgencyRadioGroup = findViewById(R.id.urgency_radio_group);

        taskToEdit = (Task) getIntent().getSerializableExtra("EXTRA_TASK_TO_EDIT");
        if (taskToEdit != null) {
            taskNameEditText.setText(taskToEdit.getName());
            taskPointsEditText.setText(String.valueOf(taskToEdit.getPoints()));
            photoRequiredSwitch.setChecked(taskToEdit.getPhotoRequired());
            // Set the radio button for urgency based on the task's urgency
            for (int i = 0; i < urgencyRadioGroup.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) urgencyRadioGroup.getChildAt(i);
                if (radioButton.getText().toString().equals(taskToEdit.getUrgency())) {
                    radioButton.setChecked(true);
                    break;
                }
            }
        }

        saveTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get name and points as strings
                String taskName = taskNameEditText.getText().toString();
                String taskPointsStr = taskPointsEditText.getText().toString();

                // Check if name is empty
                if (taskName.isEmpty()) {
                    taskNameEditText.setError("Please enter a name");
                    return;
                }

                // Check if taskPointsStr is empty
                if (taskPointsStr.isEmpty()) {
                    taskPointsEditText.setError("Please enter points");
                    return;
                }

                // Init int variable for points
                int taskPoints;

                // Define the maximum points allowed, just can't be over 2147483647 (max int value)
                int maxPoints = 1000000;

                // Try to parse the points, if it fails, show an error message
                try {
                    taskPoints = Integer.parseInt(taskPointsStr);

                    if (taskPoints > maxPoints) {
                        taskPointsEditText.setError("Points cannot be more than " + maxPoints);
                        return;
                    }

                } catch (NumberFormatException e) {
                    // Show error message if the number is too large
                    taskPointsEditText.setError("Points cannot be more than " + maxPoints);
                    return;
                }

                // Check which radio button is selected
                int selectedRadioId = urgencyRadioGroup.getCheckedRadioButtonId();
                if (selectedRadioId == -1) {
                    // No radio button is selected, show error
                    for (int i = 0; i < urgencyRadioGroup.getChildCount(); i++) {
                        RadioButton radioButton = (RadioButton) urgencyRadioGroup.getChildAt(i);
                        radioButton.setError("Please select a priority");
                    }
                    return;
                }

                // Make urgency option into a string and pass into task
                RadioButton selectedRadioButton = findViewById(selectedRadioId);
                String taskUrgency = selectedRadioButton.getText().toString();

                // Check if photo is required for task
                boolean photoRequired = photoRequiredSwitch.isChecked();

                if (taskToEdit == null) {
                    // Create a new task object
                    Task task = new Task(taskName, taskPoints, photoRequired, taskUrgency);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("EXTRA_TASK", task);
                    setResult(RESULT_OK, resultIntent);
                } else {
                    // Update the existing task object
                    taskToEdit.setName(taskName);
                    taskToEdit.setPoints(taskPoints);
                    taskToEdit.setPhotoRequired(photoRequired);
                    taskToEdit.setUrgency(taskUrgency);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("EXTRA_TASK", taskToEdit);
                    setResult(RESULT_OK, resultIntent);
                }
                finish();
            }
        });

    }
}
