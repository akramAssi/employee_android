package com.example.employee;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Objects;




public class storeService extends IntentService {

    //  action names
    public static final String ACTION_SHOW_ALL_EMPLOYEE = "displayRow";
    public static final String ACTION_DELETE_EMPLOYEE = "deleteRow";
    public static final String ACTION_INSERT_EMPLOYEE = "insertRow";
    public static final String ACTION_MODIFY_EMPLOYEE = "modifyRow";
    public static final String ACTION_SEARCH = "searchRow";

    //  parameters

    public static final String info = "info";
    public static final String person = "person";
    public static final String Position = "safePosition";
    public static final String salary = "salary";
    public static final String sale = "sale";
    public static final String rate = "rate";
    public static final String Search_content = "searchContent";
    public static final String Search_name = "searchName";
    public static final String Search_gender = "searchGender";
    public static final String Checked_Radio_ButtonId = "CheckedRadioButtonId";
    public static final String data = "data";
    public static final String primaryKey = "This ID is already used";


    public static myDataBase DB;
    private SQLiteDatabase db = null;

    public storeService() {
        super("storeService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        db = openOrCreateDatabase("employee", MODE_PRIVATE, null);

        DB = new myDataBase(db);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_DELETE_EMPLOYEE: {
                    emp param_person = (emp) intent.getSerializableExtra(person);
                    int safePosition = intent.getIntExtra(Position, 0);
                    if (param_person != null) {
                        deleteAction(param_person, safePosition);
                        Intent i = new Intent(getApplicationContext(), myNotification.class);
                        i.setAction(ACTION_DELETE_EMPLOYEE);
                        i.putExtra(Search_name, param_person.getName());
                        startForegroundService(i);
                        break;
                    }
                }
                case ACTION_MODIFY_EMPLOYEE: {
                    emp param_person = (emp) intent.getSerializableExtra(person);
                    float param_salary = intent.getFloatExtra(salary, 0);
                    float param_sale = intent.getFloatExtra(sale, 0);
                    float param_rate = intent.getFloatExtra(rate, 0);
                    int safePosition = intent.getIntExtra(Position, 0);
                    String message = modifyAction(Objects.requireNonNull(param_person), param_salary, param_sale, param_rate, safePosition);

                    Intent i = new Intent(getApplicationContext(), myNotification.class);
                    i.setAction(ACTION_MODIFY_EMPLOYEE);
                    i.putExtra(Search_name, message);

                    startForegroundService(i);
                    break;
                }
                case ACTION_INSERT_EMPLOYEE: {
                    Object[] param_info = (Object[]) intent.getSerializableExtra(info);
                    insertAction(param_info);

                    break;
                }
                case ACTION_SEARCH: {
                    int CheckedRadioButtonId = intent.getIntExtra(Checked_Radio_ButtonId, -11);
                    String searchContent = intent.getStringExtra(Search_content);

                    Intent search = new Intent(this, searchActivity.class);

                    if (CheckedRadioButtonId == R.id.idButton) {
                        int id = Integer.parseInt(searchContent != null ? searchContent : "0");
                        search.putExtra("cox", DB.search(id));
                    } else {
                        search.putExtra("cox", DB.search(searchContent));
                    }
                    search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(search);
                    break;
                }
                case ACTION_SHOW_ALL_EMPLOYEE: {
                    String sd = intent.getStringExtra("update");
                    showAction(sd);
                    break;
                }
            }
        }
    }

    private void showAction(String sd) {
        ArrayList<emp> fg = DB.getAllData();
        Intent intent = new Intent();
        intent.setAction(ACTION_SHOW_ALL_EMPLOYEE);
        intent.putExtra("update", sd);
        intent.putExtra(data, fg);
        sendBroadcast(intent);
    }

    private String modifyAction(emp param_person, float param_salary, float param_sale, float param_rate, int safePosition) {
        DB.update(param_person.getId(), param_salary, param_sale, param_rate);

        Intent intent = new Intent();
        intent.setAction(ACTION_MODIFY_EMPLOYEE);
        intent.putExtra(Position, safePosition);
        intent.putExtra(salary, param_salary);
        intent.putExtra(sale, param_sale);
        intent.putExtra(rate, param_rate);
        sendBroadcast(intent);
        float f_salary = param_person.getSalary() - param_salary;
        float f_sale = param_person.getSales() - param_sale;
        float f_rate = param_person.getRate() - param_rate;

        if (f_rate == 0 && f_sale == 0 && f_salary == 0)
            return "no value change for " + param_person.getName();
        return "modify : " + (f_salary != 0.0f ? "salary -> " + param_salary : " ")
                + (f_sale != 0.0f ? "Tatal Sales -> " + param_sale : " ")
                + (f_rate != 0.0f ? "commission rate -> " + param_rate : " ")
                + "for " + param_person.getName() + "  Successfully";

    }


    private void deleteAction(emp param_person, int safePosition) {

        DB.delete(param_person.getId());

        Intent intent = new Intent();
        intent.setAction(ACTION_DELETE_EMPLOYEE);
        intent.putExtra(Position, safePosition);
        sendBroadcast(intent);


    }

    private void insertAction(Object[] info) {


        Intent intent = new Intent();
        intent.setAction(ACTION_INSERT_EMPLOYEE);
        sendBroadcast(intent);
        Intent i = new Intent(getApplicationContext(), myNotification.class);
        i.setAction(ACTION_INSERT_EMPLOYEE);
        if (DB.insert(info)) {

            i.putExtra(Search_name, (String) info[1]);
            i.putExtra(Search_gender, (char) info[2]);
        } else {
            i.putExtra(Search_name, primaryKey);
        }
        startForegroundService(i);
    }
}


