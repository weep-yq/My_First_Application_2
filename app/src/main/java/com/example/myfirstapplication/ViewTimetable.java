package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.lang.Runnable;

public class ViewTimetable extends AppCompatActivity {
    public static Timetable confirmedTT;
    public TextView timetableTextView;
    public Button viewButton;
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_timetable);
        viewButton = (Button) findViewById(R.id.viewButton);
        timetableTextView = (TextView) findViewById(R.id.timetableTextView);
    }

    //background thread of generating the timetable
    public void startThread(View view) {
        generateTimetableRunnable runnable = new generateTimetableRunnable();
        new Thread(runnable).start();
    }

    //background runnable of generating the timetable
    class generateTimetableRunnable implements Runnable {

        @Override
        public void run() {
            int numberOfModules = EditModules.listOfUserInput.size();

            //get information from the prev activity
            String conditionLessonModuleCode = SetRequirement.modEditText.getText().toString();
            String conditionLessonNum = SetRequirement.lessonCodeEditText.getText().toString();
            String conditionLessonType = SetRequirement.typeOfLessonEditText.getText().toString();

            Module[] modulesTaking = new Module[numberOfModules];
            List<AllLesson> listOfLectures = new ArrayList<>();
            List<AllLesson> listOfLabs = new ArrayList<>();
            List<AllLesson> listOfSectionals = new ArrayList<>();
            List<AllLesson> listOfRecitations = new ArrayList<>();
            List<AllLesson> listOfTutorials = new ArrayList<>();
            boolean hasError = false;
            Data data = new Data();

            //EDIT: shifted the creation of tt to here to add in the lesson in the condition
            Timetable tt = new Timetable(1);

            for (int i = 0; i < numberOfModules; ++i) {
                String newModuleCode = EditModules.listOfUserInput.get(i);
                Module newModule = data.get(newModuleCode);

                if (newModule == null) {
                    hasError = true;
                    System.out.println("Null Module Error");
                    break;
                } else {
                    modulesTaking[i] = newModule;

                    /*New 1*/
                    if (newModule.getCode().equals(conditionLessonModuleCode)) {
                        List<Lesson> conditionLesson = newModule.getLesson(conditionLessonNum, conditionLessonType);
                        if (conditionLesson.isEmpty()) {
                            hasError = true;
                            System.out.println("Error");
                            break;
                        } else {
                            for (Lesson lesson : conditionLesson) {
                                if (tt.check(lesson)) {
                                    tt.add(lesson);
                                } else {
                                    hasError = true;
                                    System.out.println("Error");
                                    break;
                                }
                            }
                            //Empty the All" " list in the module
                            boolean updateBoolean = newModule.updateAllLesson(conditionLessonType);
                            if (! updateBoolean) {
                                hasError = true;
                                System.out.println("Update Error");
                            }
                        }
                    }
                    /*End of New 1*/

                    if (!newModule.getLect().isEmpty()) {
                        listOfLectures.add(newModule.getLect());
                    }

                    if (!newModule.getLab().isEmpty()) {
                        listOfLabs.add(newModule.getLab());
                    }

                    if (!newModule.getSec().isEmpty()) {
                        listOfSectionals.add(newModule.getSec());
                    }

                    if (!newModule.getRec().isEmpty()) {
                        listOfRecitations.add(newModule.getRec());
                    }

                    if (!newModule.getTut().isEmpty()) {
                        listOfTutorials.add(newModule.getTut());
                    }
                }
            }

            if (!hasError) {
                //EDIT: Shifted up
                //Timetable tt = new Timetable(1);

                LessonSimulator simulator = new LessonSimulator(listOfLectures, listOfLabs, listOfTutorials, listOfRecitations, listOfSectionals, tt);
                confirmedTT = simulator.generate(null);
                if (confirmedTT.getPossible()) {
                    System.out.println(confirmedTT);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timetableTextView.setText(confirmedTT.toString());
                        }
                    });
                } else {
                    //System.out.println(confirmedTT);
                    System.out.println("Error");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timetableTextView.setText(confirmedTT.toString());
                        }
                    });
                }
            }
        }
    }
}





