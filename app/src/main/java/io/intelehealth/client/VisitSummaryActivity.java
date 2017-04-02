package io.intelehealth.client;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.intelehealth.client.objects.Node;
import io.intelehealth.client.objects.Obs;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.WebResponse;
import io.intelehealth.client.services.ClientService;

public class VisitSummaryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient Summary";


    //Change when used with a different organization.
    //This is a demo server.

    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2;
    String identifierNumber;

    boolean uploaded = false;
    boolean dataChanged = false;
    String failedMessage;

    Context context;

    String patientID;
    String visitID;
    String state;
    String patientName;
    String intentTag;
    String visitUUID;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    Patient patient = new Patient();
    Obs complaint = new Obs();
    Obs famHistory = new Obs();
    Obs patHistory = new Obs();
    Obs physFindings = new Obs();
    Obs height = new Obs();
    Obs weight = new Obs();
    Obs pulse = new Obs();
    Obs bpSys = new Obs();
    Obs bpDias = new Obs();
    Obs temperature = new Obs();
    Obs spO2 = new Obs();

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;

    TextView nameView;
    TextView idView;
    TextView heightView;
    TextView weightView;
    TextView pulseView;
    TextView bpView;
    TextView tempView;
    TextView spO2View;
    TextView bmiView;
    TextView complaintView;
    TextView famHistView;
    TextView patHistView;
    TextView physFindingsView;

    String medHistory;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    Button uploadButton;
    Button downloadButton;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_visit_summary, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        //do nothing
        //Use the buttons on the screen to navigate
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//            case R.id.summary_home:
//                endVisit();
//                return true;
            case R.id.summary_print:
                doWebViewPrint();
                return true;
            case R.id.summary_endVisit:
                endVisit();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
//            Log.v(LOG_TAG, "Patient ID: " + patientID);
//            Log.v(LOG_TAG, "Visit ID: " + visitID);
//            Log.v(LOG_TAG, "Patient Name: " + patientName);
//            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        } else {
            patientID = "AND1";
            visitID = "6";
            state = "";
            patientName = "John Alam";
            intentTag = "";
        }


        String titleSequence = patientName + ": " + getTitle();
        setTitle(titleSequence);

        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        identifierNumber = patientID;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mLayout = (LinearLayout) findViewById(R.id.summary_layout);
        context = getApplicationContext();


        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        editVitals = (ImageButton) findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = (ImageButton) findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = (ImageButton) findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = (ImageButton) findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = (ImageButton) findViewById(R.id.imagebutton_edit_pathist);


        uploadButton = (Button) findViewById(R.id.button_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Uploading to doctor.", Snackbar.LENGTH_LONG).show();

                Intent serviceIntent = new Intent(VisitSummaryActivity.this, ClientService.class);
                serviceIntent.putExtra("serviceCall", "visit");
                serviceIntent.putExtra("patientID", patientID);
                serviceIntent.putExtra("visitID", visitID);
                serviceIntent.putExtra("name", patientName);
                startService(serviceIntent);

                //mLayout.removeView(uploadButton);

                downloadButton = new Button(VisitSummaryActivity.this);
                downloadButton.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                downloadButton.setText(R.string.visit_summary_button_download);

                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "Downloading from doctor", Snackbar.LENGTH_LONG).show();
                        retrieveOpenMRS(view);
                    }
                });

                mLayout.addView(downloadButton, 0);

            }
        });

        if (intentTag != null && intentTag.equals("prior")) {
            uploadButton.setEnabled(false);
        }


        queryData(String.valueOf(patientID));
        nameView = (TextView) findViewById(R.id.textView_name_value);
        idView = (TextView) findViewById(R.id.textView_id_value);

        nameView.setText(patientName);
        idView.setText(patientID);

        heightView = (TextView) findViewById(R.id.textView_height_value);
        weightView = (TextView) findViewById(R.id.textView_weight_value);
        pulseView = (TextView) findViewById(R.id.textView_pulse_value);
        bpView = (TextView) findViewById(R.id.textView_bp_value);
        tempView = (TextView) findViewById(R.id.textView_temp_value);
        spO2View = (TextView) findViewById(R.id.textView_pulseox_value);
        bmiView = (TextView) findViewById(R.id.textView_bmi_value);
        complaintView = (TextView) findViewById(R.id.textView_content_complaint);
        famHistView = (TextView) findViewById(R.id.textView_content_famhist);
        patHistView = (TextView) findViewById(R.id.textView_content_pathist);
        physFindingsView = (TextView) findViewById(R.id.textView_content_physexam);

        heightView.setText(height.getValue());
        weightView.setText(weight.getValue());
        pulseView.setText(pulse.getValue());
        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        bpView.setText(bpText);

        Double mWeight = Double.parseDouble(weight.getValue());
        Double mHeight = Double.parseDouble(height.getValue());

        double numerator = mWeight * 10000;
        double denominator = (mHeight) * (mHeight);
        double bmi_value = numerator / denominator;
        mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);


        bmiView.setText(mBMI);
        tempView.setText(temperature.getValue());
        spO2View.setText(spO2.getValue());
        complaintView.setText(complaint.getValue());
        famHistView.setText(famHistory.getValue());

        patHistory.setValue(medHistory);
        patHistView.setText(patHistory.getValue());
        physFindingsView.setText(physFindings.getValue());

        /*
        final CardView complaintCard = (CardView) findViewById(R.id.cardView_complaint);
        complaintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        */

        //editComplaint.setVisibility(View.GONE);
        editVitals.setVisibility(View.GONE);
        editPhysical.setVisibility(View.GONE);
        editFamHist.setVisibility(View.GONE);
        editMedHist.setVisibility(View.GONE);

        editComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder complaintDialog = new AlertDialog.Builder(VisitSummaryActivity.this);
                complaintDialog.setTitle("Complaint Observed");
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                complaintDialog.setView(convertView);

                final EditText complaintText = (EditText) convertView.findViewById(R.id.textView_entry);
                complaintText.setText(complaint.getValue());
                complaintText.setEnabled(false);

                complaintDialog.setPositiveButton("Manual Entry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
                        textInput.setTitle(R.string.question_text_input);
                        final EditText dialogEditText = new EditText(context);
                        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        dialogEditText.setText(complaint.getValue());
                        textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //TODO: take the new text, and update the specific line in the database
                                complaint.setValue(dialogEditText.getText().toString());

                                dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //TODO: do nothing

                                dialog.dismiss();
                            }
                        });
                        textInput.show();
                    }
                });

                complaintDialog.setNeutralButton("Erase and Re-Do", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(VisitSummaryActivity.this, ComplaintNodeActivity.class);
                        intent1.putExtra("patientID", patientID);
                        intent1.putExtra("visitID", visitID);
                        intent1.putExtra("name", patientName);
                        intent1.putExtra("tag", "edit");
                        startActivity(intent1);
                        dialogInterface.dismiss();
                        //TODO: this time, when the edit tag is found, make the complaintnodeact update instead of insert

                    }
                });

                complaintDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


            complaintDialog.show();
            }
        });


/*
    public static void subAskNumber(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final AlertDialog.Builder numberDialog = new AlertDialog.Builder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
        final NumberPicker numberPicker = (NumberPicker) convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(100);
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());
                if(node.getLanguage().contains("_")){
                    node.setLanguage(node.getLanguage().replace("_", value));
                } else {
                    node.addLanguage(" " + value);
                    node.setText(value);
                    //node.setText(node.getLanguage());
                }
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        numberDialog.show();

    }

        */
        /*

        cardView_vitals
        cardView_complaint
        cardView_physexam
        cardView_pathist
        cardView_famhist

         */

    }


    public void retrieveOpenMRS(View view) {
        new RetrieveData(this).execute();
    }

    private void endVisit() {
        String[] columnsToReturn = {"openmrs_visit_uuid"};
        String visitIDorderBy = "start_datetime";
        String visitIDSelection = "_id = ?";
        String[] visitIDArgs = {visitID};
        final Cursor visitIDCursor = db.query("visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        if (visitIDCursor != null && visitIDCursor.getCount() > 0 && visitIDCursor.moveToLast()) {
            visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("openmrs_visit_uuid"));
            visitIDCursor.close();
        }
        if (visitUUID == null) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please upload first before attempting to end the visit.");
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else {
            Intent serviceIntent = new Intent(VisitSummaryActivity.this, ClientService.class);
            serviceIntent.putExtra("serviceCall", "endVisit");
            serviceIntent.putExtra("patientID", patientID);
            serviceIntent.putExtra("visitUUID", visitUUID);
            serviceIntent.putExtra("name", patientName);
            startService(serviceIntent);


            Intent intent = new Intent(VisitSummaryActivity.this, HomeActivity.class);
            startActivity(intent);
        }


    }

    public void queryData(String dataString) {

        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {dataString};

        String table = "patient";
        String[] columnsToReturn = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province", "country",
                "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setFirstName(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddleName(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLastName(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDateOfBirth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndex("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndex("address2")));
                patient.setCityVillage(idCursor.getString(idCursor.getColumnIndex("city_village")));
                patient.setStateProvince(idCursor.getString(idCursor.getColumnIndex("state_province")));
                patient.setCountry(idCursor.getString(idCursor.getColumnIndex("country")));
                patient.setPostalCode(idCursor.getString(idCursor.getColumnIndex("postal_code")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndex("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient.setPatientPhoto(idCursor.getString(idCursor.getColumnIndex("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();


        String[] columns = {"value", " concept_id"};
        String orderBy = "visit_id";

        try {
            String famHistSelection = "patient_id = ? AND concept_id = ?";
            String[] famHistArgs = {dataString, "163188"};
            Cursor famHistCursor = db.query("obs", columns, famHistSelection, famHistArgs, null, null, orderBy);
            famHistCursor.moveToLast();
            String famHistText = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistory.setValue(famHistText);
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            famHistory.setValue(""); // if family history does not exist
        }

        try {
            String medHistSelection = "patient_id = ? AND concept_id = ?";
            String[] medHistArgs = {dataString, "163187"};
            Cursor medHistCursor = db.query("obs", columns, medHistSelection, medHistArgs, null, null, orderBy);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            patHistory.setValue(medHistText);
            if (!medHistText.isEmpty()) {
                medHistory = patHistory.getValue();
                medHistory = medHistory.replace("\"", "");
                medHistory = medHistory.replace("\n", "");
                do {
                    medHistory = medHistory.replace("  ", "");
                } while (medHistory.contains("  "));
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            patHistory.setValue(""); // if medical history does not exist
        }

        String visitSelection = "patient_id = ? AND visit_id = ?";
        String[] visitArgs = {dataString, visitID};
        Cursor visitCursor = db.query("obs", columns, visitSelection, visitArgs, null, null, orderBy);
        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndex("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(int concept_id, String value) {
        switch (concept_id) {
            case 163186: //Current Complaint
                complaint.setValue(value);
                break;
            case 163189: //Physical Examination
                physFindings.setValue(value);
                break;
            case 5090: //Height
                height.setValue(value);
                break;
            case 5089: //Weight
                weight.setValue(value);
                break;
            case 5087: //Pulse
                pulse.setValue(value);
                break;
            case 5085: //Systolic BP
                bpSys.setValue(value);
                break;
            case 5086: //Diastolic BP
                bpDias.setValue(value);
                break;
            case 163202: //Temperature
                temperature.setValue(value);
                break;
            case 5092: //SpO2
                spO2.setValue(value);
                break;
            default:
                break;

        }
    }

    private void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Patient WebView", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        String mPatientName = patient.getFirstName() + " " + patient.getMiddleName() + " " + patient.getLastName();
        String mPatientDob = patient.getDateOfBirth();
        String mAddress = patient.getAddress1() + "\n" + patient.getAddress2();
        String mCityState = patient.getCityVillage();
        String mPhone = patient.getPhoneNumber();
        String mState = patient.getStateProvince();
        String mCountry = patient.getCountry();

        String mSdw = patient.getSdw();
        String mOccupation = patient.getOccupation();
        String mGender = patient.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String mDate = df.format(c.getTime());

        String mPatHist = patHistory.getValue();
        String mFamHist = famHistory.getValue();
        mHeight = height.getValue();
        mWeight = weight.getValue();
        mBP = bpSys.getValue() + "/" + bpDias.getValue();
        mPulse = pulse.getValue();
        mTemp = temperature.getValue();
        mSPO2 = spO2.getValue();
        String mComplaint = complaint.getValue();
        String mExam = physFindings.getValue();


        // Generate an HTML document on the fly:
        String htmlDocument =
                String.format("<h1 id=\"intelecare-patient-detail\">Intelehealth Visit Summary</h1>\n" +
                                "<h1>%s</h1>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<h2 id=\"patient-information\">Patient Information</h2>\n" +
                                "<ul>\n" +
                                "<li>%s</li>\n" +
                                "<li>Son/Daughter/Wife of: %s</li>\n" +
                                "<li>Occupation: %s</li>\n" +
                                "</ul>\n" +
                                "<h2 id=\"address-and-contact\">Address and Contact</h2>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<h2 id=\"vitals\">Vitals</h2>\n" +
                                "<li>Height: %s</li>\n" +
                                "<li>Weight: %s</li>\n" +
                                "<li>BMI: %s</li>\n" +
                                "<li>Blood Pressure: %s</li>\n" +
                                "<li>Pulse: %s</li>\n" +
                                "<li>Temperature: %s</li>\n" +
                                "<li>SpO2: %s</li>\n" +
                                "<h2 id=\"patient-history\">Patient History</h2>\n" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"family-history\">Family History</h2>\n" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Complaint and Observations</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"examination\">On Examination</h2>" +
                                "<p>%s</p>\n" +
                                "<h2 id=\"complaint\">Diagnosis</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Prescription</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Tests To Be Performed</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">General Advices</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Doctor's Name</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Additional Comments</h2>" +
                                "<li>%s</li>\n",
                        mPatientName, patientID, mDate, mPatientDob, mSdw, mOccupation, mAddress, mCityState, mPhone, mHeight, mWeight,
                        mBMI, mBP, mPulse, mTemp, mSPO2, mPatHist, mFamHist, mComplaint, mExam, diagnosisReturned, rxReturned, testsReturned, adviceReturned, doctorName, additionalReturned);
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Visit Summary";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    private class RetrieveData extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = RetrieveData.class.getSimpleName();

        private final Context context;

        public RetrieveData(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(String... params) {
            String queryString = "?q=" + identifierNumber;
            //Log.d(LOG_TAG, identifierNumber);
            WebResponse responseEncounter;
            responseEncounter = HelperMethods.getCommand("encounter", queryString, getApplicationContext());
            if (responseEncounter != null && responseEncounter.getResponseCode() != 200) {
//                failedStep("Encounter search was unsuccessful");
                //Log.d(LOG_TAG, "Encounter searching was unsuccessful");
                return null;
            }


            assert responseEncounter != null;
            JSONArray resultsArray = null;
            List<String> uriList = new ArrayList<>();
            try {
                JSONObject JSONResponse = new JSONObject(responseEncounter.getResponseString());
                resultsArray = JSONResponse.getJSONArray("results");


                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                Date todayDate = new Date();
                String thisDate = currentDate.format(todayDate);

                Log.d(LOG_TAG, thisDate);

                String searchString = "Visit Note " + thisDate;

                if (resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject checking = resultsArray.getJSONObject(i);
                        if (checking.getString("display").equals(searchString)) {
                            uriList.add("/" + checking.getString("uuid"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            List<WebResponse> obsResponse = new ArrayList<>();
            for (int i = 0; i < uriList.size(); i++) {
                obsResponse.add(i, HelperMethods.getCommand("encounter", uriList.get(i), getApplicationContext()));
                if (obsResponse.get(i) != null && obsResponse.get(i).getResponseCode() != 200) {
                    String errorMessage = "Obs get call number " + String.valueOf(i) + " of " + String.valueOf(uriList.size()) + " was unsuccessful";
//                    failedStep(errorMessage);
//                    Log.d(LOG_TAG, errorMessage);
                    return null;
                }
            }


            JSONObject responseObj;
            JSONArray obsArray;
            JSONArray providersArray;

            for (int i = 0; i < obsResponse.size(); i++) {
                //Log.d(LOG_TAG, obsResponse.get(i).toString());
                //Log.d(LOG_TAG, obsResponse.get(i).getResponseString());

                try {
                    responseObj = new JSONObject(obsResponse.get(i).getResponseString());
                    obsArray = responseObj.getJSONArray("obs");
                    providersArray = responseObj.getJSONArray("encounterProviders");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }

                //Log.d(LOG_TAG, obsArray.toString());
                for (int k = 0; k < obsArray.length(); k++) {
                    String obsString = "";
                    //Log.d(LOG_TAG, obsString);
                    try {
                        JSONObject obsObj = obsArray.getJSONObject(k);
                        obsString = obsObj.getString("display");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }

                    String index = obsString.substring(0, obsString.indexOf(":"));
                    String indexText = obsString.substring(obsString.indexOf(":") + 1, obsString.length());

                    if (index.contains("TELEMEDICINE DIAGNOSIS")) {
                        if (!diagnosisReturned.contains(indexText) && !diagnosisReturned.isEmpty()) {
                            diagnosisReturned = diagnosisReturned + "\n" + indexText;
                        } else {
                            diagnosisReturned = indexText;
                        }
                    }

                    if (index.contains("JSV MEDICATIONS")) {
                        if (!rxReturned.contains(indexText) && !rxReturned.isEmpty()) {
                            rxReturned = rxReturned + "\n" + indexText;
                        } else {
                            rxReturned = indexText;
                        }

                    }

                    if (index.contains("MEDICAL ADVICE")) {
                        if (!adviceReturned.contains(indexText) && !adviceReturned.isEmpty()) {
                            adviceReturned = adviceReturned + "\n" + indexText;
                        } else {
                            adviceReturned = indexText;
                        }

                    }

                    if (index.contains("REQUESTED TESTS")) {
                        if (!testsReturned.contains(indexText) && !testsReturned.isEmpty()) {
                            testsReturned = testsReturned + "\n" + indexText;
                        } else {
                            testsReturned = indexText;
                        }

                    }

                    if (index.contains("Additional Comments")) {
                        if (!additionalReturned.contains(indexText) && !additionalReturned.isEmpty()) {
                            additionalReturned = additionalReturned + "\n" + indexText;
                        } else {
                            additionalReturned = indexText;
                        }

                    }

                }

                for (int j = 0; j < providersArray.length(); j++) {
                    String providerName;

                    try {
                        JSONObject providerObj = providersArray.getJSONObject(j);
                        providerName = providerObj.getString("display");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }

                    String[] providerSplit = providerName.split(":");
                    providerName = providerSplit[0];
                    if (!doctorName.contains(providerName) && !doctorName.isEmpty()) {
                        doctorName = doctorName + "\n" + providerName;
                    } else {
                        doctorName = providerName;
                    }

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (!doctorName.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_doctor_details), doctorName, 0);

            }
            if (!additionalReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_additional_comments), additionalReturned, 0);

            }

            if (!testsReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_tests_prescribed), testsReturned, 0);

            }

            if (!adviceReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_advice), adviceReturned, 0);

            }
            if (!rxReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_rx), rxReturned, 0);

            }
            if (!diagnosisReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_diagnosis), diagnosisReturned, 0);

            }

            mLayout.removeView(downloadButton);

            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    private void createNewCardView(String title, String content, int index) {
        final LayoutInflater inflater = VisitSummaryActivity.this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.card_doctor_content, null);
        TextView titleView = (TextView) convertView.findViewById(R.id.textview_heading);
        TextView contentView = (TextView) convertView.findViewById(R.id.textview_content);
        titleView.setText(title);
        contentView.setText(content);
        mLayout.addView(convertView, index);
    }
}