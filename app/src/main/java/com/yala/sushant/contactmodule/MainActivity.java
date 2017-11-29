package com.yala.sushant.contactmodule;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    Button btnContact;
    TextView txtViewContact;




    ArrayList<String> contactList;
    Cursor cursor;
    int counter;
    ArrayList<String> allUserPhoneNumber;
    ArrayList<String> allUserUserId;
    //FirebaseUserSync
    private DatabaseReference rootData = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainActivity temp = this;
        MainActivityPermissionsDispatcher.contactReadWriteNeedPermissionWithCheck(temp);
        btnContact = (Button) findViewById(R.id.btn_getContact);
        txtViewContact = (TextView) findViewById(R.id.list);

        Toast.makeText(getApplicationContext(), "EWBOK", Toast.LENGTH_SHORT).show();

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<PhoneContact> showListContact = new ArrayList<PhoneContact>();
                showListContact = getContacts();
                // starting sync
                startSync(showListContact);


                //tobeRemoved
                //remove Start
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < showListContact.size(); i++) {
                    sb.append(i + " ");
                    sb.append(showListContact.get(i).getName());
                    sb.append(" Phone: ");
                    for (int j = 0; j < showListContact.get(i).getPhone().size(); j++) {
                        sb.append(showListContact.get(i).getPhone().get(j));
                        sb.append(" - ");
                    }
                    sb.append(" ");
                    sb.append(showListContact.get(i).getDob());
                    sb.append("\n");
                }
                txtViewContact.setText(sb.toString());
                //remove End
            }
        });
    }


    // startSync
    // startSync()=>getMutualContact()=>saveSyncContact()
    public void startSync(final ArrayList<PhoneContact> phoneContact) {

        allUserPhoneNumber = new ArrayList<>();
        allUserUserId = new ArrayList<>();
        DatabaseReference getUserList = rootData.child("User");
        getUserList.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allUserPhoneNumber.clear();
                allUserUserId.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    String phone = userSnapshot.child("phone").getValue().toString();
                    String userId = userSnapshot.child("user").getValue().toString();
                    allUserPhoneNumber.add(phone);
                    allUserUserId.add(userId);
                    Toast.makeText(getApplicationContext(), phone, Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(getApplicationContext(), Integer.toString(allUserPhoneNumber.size()), Toast.LENGTH_SHORT).show();


                //StartSync
                Toast.makeText(getApplicationContext(), "Start Sync", Toast.LENGTH_SHORT).show();

                //mutualContact
                ArrayList<MutualContact> finalSyncList = getMutualContact(phoneContact, allUserPhoneNumber, allUserUserId);

                //savingDataToFirebase
                saveSyncContact(finalSyncList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // returns mutual arrayList
    private ArrayList<MutualContact> getMutualContact(ArrayList<PhoneContact> phoneContact, ArrayList<String> allUserPhoneNumber, ArrayList<String> allUserUserId) {

        ArrayList<MutualContact> returnMutualArray = new ArrayList<>();

        for (int i = 0; i < phoneContact.size(); i++) {

            outerloop:
            if (phoneContact.get(i).getPhone().size() > 1) {

                for (int k = 0; k < phoneContact.get(i).getPhone().size(); k++) {
                    for (int j = 0; j < allUserPhoneNumber.size(); j++) {
                        StringBuilder tempCheck = new StringBuilder();

                        if (!phoneContact.get(i).getPhone().get(k).toString().startsWith("+")) {

                            tempCheck.append("+977");
                            tempCheck.append(phoneContact.get(i).getPhone().get(k).toString());
                        } else {

                            tempCheck.append(phoneContact.get(i).getPhone().get(k).toString());
                        }

                        if (tempCheck.toString().equals(allUserPhoneNumber.get(j).toString())) {

                            MutualContact temp = new MutualContact();
                            temp.setName(phoneContact.get(i).getName());
                            temp.setPhone(allUserPhoneNumber.get(j).toString());
                            temp.setUserId(allUserUserId.get(j));
                            returnMutualArray.add(temp);
                            //break
                            break outerloop;
                        }
                    }
                }
            } else {
                StringBuilder tempCheck = new StringBuilder();

                if (!phoneContact.get(i).getPhone().get(0).toString().startsWith("+")) {
                    //  Toast.makeText(this, "Trim "+phoneContact.get(i).getPhone().get(0).toString(), Toast.LENGTH_SHORT).show();
                    tempCheck.append("+977");
                    tempCheck.append(phoneContact.get(i).getPhone().get(0).toString());
                } else {
                    tempCheck.append(phoneContact.get(0).getPhone().get(0).toString());
                }
                // Toast.makeText(this, "Trimmed"+tempCheck.toString(), Toast.LENGTH_SHORT).show();
                for (int j = 0; j < allUserPhoneNumber.size(); j++) {

                    if (tempCheck.toString().equals(allUserPhoneNumber.get(j))) {

                        MutualContact temp = new MutualContact();
                        temp.setUserId(allUserUserId.get(j));
                        temp.setPhone(allUserPhoneNumber.get(j));
                        temp.setName(phoneContact.get(i).getName());
                        returnMutualArray.add(temp);
                    }
                }
            }
        }
        return returnMutualArray;
    }


    // saving data to firebase
    private void saveSyncContact(ArrayList<MutualContact> syncedArrayList) {

        DatabaseReference databaseReferenceContact;

        //to be replced by Auth users userId
        String SelfId = "selfidtobegetFromAuth";
        databaseReferenceContact = FirebaseDatabase.getInstance().getReference("Contacts").child(SelfId);

        for (int i = 0; i < syncedArrayList.size(); i++) {
            databaseReferenceContact.child(syncedArrayList.get(i).getUserId()).setValue(syncedArrayList.get(i));
            Toast.makeText(this, "Synced:" + syncedArrayList.get(i).getName() + " Phone:" + syncedArrayList.get(i).getPhone(), Toast.LENGTH_SHORT).show();

        }

    }


    // returns contactList from phone
    private ArrayList<PhoneContact> getContacts() {

        ArrayList<PhoneContact> returnPhoneContacts = new ArrayList<>();

        contactList = new ArrayList<String>();

//        PhoneContact phoneContact= new PhoneContact();


        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output;

        ContentResolver contentResolver = getContentResolver();
        int i = 0;

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            counter = 0;


            while (cursor.moveToNext()) {
                output = new StringBuffer();
                PhoneContact phoneContact = new PhoneContact();


                // Update the progress message
//                updateBarHandler.post(new Runnable() {
//                    public void run() {
//                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
//                    }
//                });

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    //Single object for PhoneContact To be added on arrayList

                    output.append("\n First Name:" + name);
                    phoneContact.setName(name);


                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);


                    ArrayList<String> phoneListOfContact = new ArrayList<>();
                    phoneListOfContact.clear();
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append("\n Phone number:" + phoneNumber);
                        phoneListOfContact.add(phoneNumber);

                    }

                    phoneContact.setPhone(phoneListOfContact);

                    phoneCursor.close();

                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (emailCursor.moveToNext()) {

                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));

                        output.append("\n Email:" + email);

                    }

                    emailCursor.close();

                    String columns[] = {
                            ContactsContract.CommonDataKinds.Event.START_DATE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.MIMETYPE,
                    };

                    String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                            " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contact_id;

                    String[] selectionArgs = null;
                    String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;

                    Cursor birthdayCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
                    Log.d("BDAY", birthdayCur.getCount() + "");
                    if (birthdayCur.getCount() > 0) {
                        while (birthdayCur.moveToNext()) {
                            String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                            output.append("Birthday :" + birthday);
                            phoneContact.setDob(birthday);
                        }
                    }
                    Log.d("BDAY", "Username:" + name + " UserPhone: " + phoneNumber + " count     " + i);
                    birthdayCur.close();

                    returnPhoneContacts.add(phoneContact);
                }

                // Add the contact to the ArrayList
                contactList.add(output.toString());
//                returnPhoneContacts.add(phoneContact);
            }

            // ListView has to be updated using a ui thread
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, contactList);
//
//                }
//            });

//            // Dismiss the progressbar after 500 millisecondds
//            updateBarHandler.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    pDialog.cancel();
//                }
//            }, 500);
        }


        //End
        //  Toast.makeText(this, returnPhoneContacts.get(0).getName() + "  " + returnPhoneContacts.get(0).getPhone().get(0), Toast.LENGTH_SHORT).show();
        return returnPhoneContacts;
    }







    @NeedsPermission({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void contactReadWriteNeedPermission() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void contactReadWriteOnShowRationale(final PermissionRequest request) {
    }

    @OnPermissionDenied({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void contactReadWriteOnPermissionDenied() {
    }

    @OnNeverAskAgain({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void contactReadWriteOnNeverAskAgain() {
    }


}
