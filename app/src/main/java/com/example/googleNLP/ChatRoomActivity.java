package com.example.googleNLP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.googleNLP.Adapter.MessageAdapter;
import com.example.googleNLP.Model.Chat;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Entity;
import com.google.api.services.language.v1beta2.model.Features;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChatRoomActivity extends AppCompatActivity {

    private final String CLOUD_API_KEY = "YOUR_API_KEY";

    private static final String TAG = "ChatRoomActivity";
    static final int CUSTOM_POST_REQUEST = 1;  // The request code
    static float sentimentScore = 0;

    ImageButton btn_input;
    EditText ed_input;
    TextView response;
    String userQuery;
    MessageAdapter messageAdapter;
    ArrayList<Chat> mchat = new ArrayList<>();

    RecyclerView recyclerView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        btn_input = (ImageButton)findViewById(R.id.btn_input);
        ed_input = (EditText) findViewById(R.id.ed_input);
        response = (TextView) findViewById(R.id.tv_response);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        btn_input.setOnClickListener(doClick);
    }

    private Button.OnClickListener doClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

            //POST Request
            sendMessage();
        }
    };

    private void sendMessage() {

        try {
            userQuery = ed_input.getText().toString();


            Log.d(TAG, "User Query: " + userQuery);
            mchat.add(new Chat("sender", userQuery));

            messageAdapter = new MessageAdapter(ChatRoomActivity.this, mchat);

            recyclerView.setAdapter(messageAdapter);


            NLPclass nlPclass = new NLPclass();
            nlPclass.execute();

            ed_input.setText("");


        }catch (ActivityNotFoundException e){
            Toast.makeText(this, "POST Request error", Toast.LENGTH_SHORT).show();
        }
    }




    private String NLPprocessing(){

//        需要HTTP傳輸和JSON工廠這兩個參數, 通過分配CloudNaturalLanguageRequestInitializer實例給它，可以強制它將API金鑰添加到所有請求中
        final CloudNaturalLanguage naturalLanguageService = new CloudNaturalLanguage.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                .setCloudNaturalLanguageRequestInitializer(new CloudNaturalLanguageRequestInitializer(CLOUD_API_KEY)).build();

//        要使用API​​分析的所有文本必須放在Document物件內
        String transcript = ed_input.getText().toString();
        Document document = new Document();
        document.setType("PLAIN_TEXT");
        document.setLanguage("zh-Hant");
        document.setContent(transcript);

        Features features = new Features();
        features.setExtractEntities(true);  //提取實體
        features.setExtractDocumentSentiment(true); //情緒分析
        features.setExtractSyntax(true);
//        features.setExtractEntitySentiment(true);

        final AnnotateTextRequest request = new AnnotateTextRequest();
        request.setDocument(document);
        request.setFeatures(features);

        AnnotateTextResponse response = null;
        try {
            response = naturalLanguageService.documents().annotateText(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

        final List<Entity> entityList = response.getEntities();
        final float sentiment = response.getDocumentSentiment().getScore();
        sentimentScore = response.getDocumentSentiment().getScore();

        return String.valueOf(sentimentScore);

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                //Instantiates a client
//
//                AnnotateTextResponse response = null;
//                try {
//                    response = naturalLanguageService.documents().annotateText(request).execute();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                final List<Entity> entityList = response.getEntities();
//                final float sentiment = response.getDocumentSentiment().getScore();
//                sentimentScore = response.getDocumentSentiment().getScore();
////                final List<Sentence> sentenceList = response.getSentences();
//                final List<Token> tokenList = response.getTokens();

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String entities = "";
//                        for (Entity entity : entityList) {
//                            entities += "\n" + entity.getName().toUpperCase();
//                        }
//
//                        String tokens = "";
//                        for (Token token: tokenList) {
//                            tokens += "\n" + token.getLemma();
//                        }
//
//                        AlertDialog dialog = new AlertDialog.Builder(ChatRoomActivity.this)
//                                .setTitle("Sentiment: " + sentiment)
//                                .setMessage("This message talks about: " + entities + " tokens = " + tokens)
//                                .setNeutralButton("OK", null)
//                                .create();
//                        dialog.show();
//                    }
//                });

//            }
//        });

    }

    class NLPclass extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {
            String s = null;
            s = NLPprocessing();

            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println("Score: " + s);
            if(Float.valueOf(s) > 0.5){
//                System.out.println("好正面: " + s);
                mchat.add(new Chat("receiver", Constants.POSITiVE_REPLY));
                messageAdapter = new MessageAdapter(ChatRoomActivity.this, mchat);
                recyclerView.setAdapter(messageAdapter);
            }else if(Float.valueOf(s) < -0.5){
//                System.out.println("好負面: " + s);
                mchat.add(new Chat("receiver", Constants.NEGATIVE_REPLY));
                messageAdapter = new MessageAdapter(ChatRoomActivity.this, mchat);
                recyclerView.setAdapter(messageAdapter);
            }else{
                RetrieveFeedTask task=new RetrieveFeedTask();
                task.execute(userQuery);
                Log.d(TAG, "AsyncTask invoked");
            }
            super.onPostExecute(s);
        }
    }




    public String GetResponse(String query) throws UnsupportedEncodingException{

        String text = "";
        BufferedReader reader;

        try{
            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Authorization", "Bearer " + "60e9b458dc2147ef94437dfdb0c62f2a");
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject jsonParam = new JSONObject();
            JSONArray queryArray = new JSONArray();
            queryArray.put(query);
            jsonParam.put("query", queryArray);

            jsonParam.put("lang", "en");
            jsonParam.put("sessionId", "1234567890");

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(jsonParam.toString());
            Log.d(TAG, "Write jsonParam");

            wr.flush();

            //Get sever response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

//            Read server Response
            while ((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }

            text = sb.toString();
            Log.d(TAG, "response is " + text);
            JSONObject object = new JSONObject(text);
            JSONObject object1 = object.getJSONObject("result");
            JSONObject fulfillment = null;
            String speech = null;
            fulfillment = object1.getJSONObject("fulfillment");
            speech = fulfillment.getString("speech");

            return speech;

        }catch (Exception e){
            Log.d(TAG, "Exception occur: " + e);

        }
        finally {
            try {

            }catch (Exception ex){

            }
        }
        return null;
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... voids) {
            String s = null;
            try{
                Log.d(TAG, "doInBackground. Param: " + voids[0]);
                s = GetResponse(voids[0]);

            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mchat.add(new Chat("receiver", s));
            messageAdapter = new MessageAdapter(ChatRoomActivity.this, mchat);
            recyclerView.setAdapter(messageAdapter);
        }
    }

}
