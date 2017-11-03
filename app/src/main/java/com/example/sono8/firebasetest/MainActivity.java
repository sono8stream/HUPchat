package com.example.sono8.firebasetest;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseDatabase database;
    DatabaseReference myRef;
    int pushCount = 0;
    List<SpannableStringBuilder> onlineMessages;
    EditText  myName,myMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//リストビュー初期化
        onlineMessages = new ArrayList<SpannableStringBuilder>();
        final ArrayAdapter<SpannableStringBuilder> arrayAdapter =
                new ArrayAdapter<SpannableStringBuilder>(
                        this, android.R.layout.simple_list_item_1, onlineMessages);
        final ListView messageListView = (ListView) findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                onlineMessages.add(
                        transformMessageFont(dataSnapshot.getValue(String.class)));
                messageListView.setAdapter(arrayAdapter);
                messageListView.setSelection(onlineMessages.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
//nameBox初期化
        myName = (EditText) findViewById(R.id.nameBox);
        myName.setText(defaultName());
        myName.setOnFocusChangeListener(new View.OnFocusChangeListener(){//フォーカス時の処理
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(myName.getText().toString().equals("Name")) {
                        myName.setText("");
                    }
                }
                else{
                    if(myName.getText().toString().length()==0){
                        myName.setText(defaultName());
                    }
                }
            }
        });
//messageBox初期化
        myMessage = (EditText) findViewById(R.id.messageBox);
        myMessage.setText(defaultMessage());
        myMessage.setOnFocusChangeListener(new View.OnFocusChangeListener(){//フォーカス時の処理
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(myMessage.getText().toString().equals("Message")) {
                        myMessage.setText("");
                    }
                }
                else{
                    if(myMessage.getText().toString().length()==0){
                        myMessage.setText(defaultMessage());
                    }
                }
            }
        });

        findViewById(R.id.sendButton).setOnClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor e = pref.edit();
        e.putString("name", myName.getText().toString());
        e.commit();
    }

    @Override
    public void onClick(View view) {
        if (view == null) return;

        switch (view.getId()) {
            case R.id.sendButton:
                sendMessage();
                break;
        }
    }

    private SpannableStringBuilder transformMessageFont(String messageText){
        SpannableStringBuilder sp=new SpannableStringBuilder();
        String[] messages=messageText.split("\t");

        sp.append(messages[0]);
        TextAppearanceSpan nameSpane=new TextAppearanceSpan(this, R.style.NameAppearance);
        sp.setSpan(nameSpane,0,sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int dateStart=sp.length();
        sp.append(" : "+messages[1]);
        TextAppearanceSpan dateSpane=new TextAppearanceSpan(this, R.style.DateAppearance);
        sp.setSpan(dateSpane,dateStart,sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int messageStart=sp.length();
        sp.append("\r\n"+messages[2]);
        TextAppearanceSpan messageSpane=new TextAppearanceSpan(this, R.style.MessageAppearance);
        sp.setSpan(messageSpane,messageStart,sp.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    private SpannableStringBuilder defaultName() {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String name = pref.getString("name", "Name");

        SpannableStringBuilder sp = new SpannableStringBuilder();
        sp.append(name);
        if (!name.equals("Name")) return sp;
        TextAppearanceSpan nameSpane = new TextAppearanceSpan(this, R.style.DefMessageAppearance);
        sp.setSpan(nameSpane, 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    private SpannableStringBuilder defaultMessage(){
        SpannableStringBuilder sp=new SpannableStringBuilder();
        sp.append("Message");
        TextAppearanceSpan nameSpane=new TextAppearanceSpan(this, R.style.DefMessageAppearance);
        sp.setSpan(nameSpane,0,sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    private String dateText(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd'  'kk':'mm':'ss");
        return sdf.format(date).toString();
    }

    private void sendMessage(){
        if (myName.getText().toString().length() == 0
                || myMessage.getText().toString().length() == 0
                || myMessage.getText().toString().equals("Message")) return;

        DatabaseReference childRef = myRef.push();
        childRef.setValue(myName.getText().toString() + "\t" + dateText() + "\t    "
                + myMessage.getText().toString());
        myMessage.setText("");
    }
}