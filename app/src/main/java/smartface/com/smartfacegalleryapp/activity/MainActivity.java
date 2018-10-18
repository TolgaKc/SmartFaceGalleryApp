package smartface.com.smartfacegalleryapp.activity;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import smartface.com.smartfacegalleryapp.R;
import smartface.com.smartfacegalleryapp.controller.ImageAdapter;
import smartface.com.smartfacegalleryapp.model.NYTimesImage;

public class MainActivity extends AppCompatActivity {

    private GridView gvImages;
    private ProgressBar progressBar;

    public static ArrayList<NYTimesImage> listImage = new ArrayList();

    //Web Service references.
    private JSONObject jsonObject;

    //My NYTimes account api key.
    private String nyTimesAPIKey = "6ac25fa1ea834d2e831bba5a7babea82";
    private String nyTimesAPIURL = "https://api.nytimes.com/svc/topstories/v2/home.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setObjects();

        //Getting the images.
        String url = nyTimesAPIURL;
        Connection con = new Connection();
        con.execute(url, null, "getNYTimesImages");
    }

    /**
     * Load objects of the view.
     */
    private void setObjects() {
        gvImages = findViewById(R.id.gvImages);
        progressBar = findViewById(R.id.progressBar);
    }

    private void showImages(ArrayList<NYTimesImage> listImage) {
        ImageAdapter imageAdapter = new ImageAdapter(this, listImage);
        gvImages.setAdapter(imageAdapter);
    }

    /**
     * My connection class for async operations.
     */
    class Connection extends AsyncTask<String, Void, String> {

        String url;
        String jsonStr;
        String callingFunction;
        int httpStatusCode;
        String httpStatusPhrase;

        Connection() {
        }

        @Override
        protected void onPreExecute() {
            //If it is visible, keep it.
            if (progressBar.getVisibility() != View.VISIBLE) {
                System.out.println("OPENING PROGRESSBAR");
                progressBar.setVisibility(View.VISIBLE);
            }

            //Clear the list before filling it with new values.
            listImage.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            url = strings[0];
            //This will be used in POST requests. Else it is null.
            jsonStr = strings[1];
            callingFunction = strings[2];

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet(url);

            //In GET requests there will be no body info. We fill header if any info exists.
            //If callingFunction needs key as header, then put it.
            if (!TextUtils.isEmpty(callingFunction)) {
                if (callingFunction.equals("getNYTimesImages")) {
                    httpGet.setHeader("api-key", nyTimesAPIKey);
                }
            }

            try {
                HttpResponse httpResponse = null;
                if (!TextUtils.isEmpty(callingFunction) && callingFunction.equals("getNYTimesImages")) {
                    httpResponse = httpClient.execute(httpGet, localContext);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

                //These will used to check if response is valid.
                httpStatusCode = httpResponse.getStatusLine().getStatusCode();//"400"
                httpStatusPhrase = httpResponse.getStatusLine().getReasonPhrase();//"Bad Request"

                String line = "";
                while ((line = rd.readLine()) != null) {
                    response += line;
                }
                System.out.println("\nREST Service Invoked Successfully..");
                System.out.println("JSON String received: " + response);
            } catch (Exception e) {
                System.out.println("\nError while calling REST Service");
                e.getLocalizedMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            System.out.println("JSONURL: " + url);
            if (!TextUtils.isEmpty(response) && httpStatusCode == 200) {
                if (!TextUtils.isEmpty(callingFunction) && callingFunction.equals("getNYTimesImages")) {
                    try {
                        jsonObject = new JSONObject(response);

                        JSONArray jsonArray;
                        JSONArray jsonArrayMultimedia;
                        JSONObject jObject;
                        String jsonString;

                        jsonArray = jsonObject.getJSONArray("results");

                        if (!TextUtils.isEmpty(jsonArray.toString())) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //Getting multimedia objects from current json.
                                jsonString = jsonArray.getJSONObject(i).getJSONArray("multimedia").toString();
                                jsonArrayMultimedia = new JSONArray(jsonString);

                                //There different size of images under a multimedia array. We select only one of the sizes.
                                for (int j = 0; j < jsonArrayMultimedia.length(); j++) {
                                    jObject = new JSONObject(jsonArrayMultimedia.get(j).toString());

                                    NYTimesImage gsonResponse = new Gson().fromJson(jObject.toString(), NYTimesImage.class);

                                    //Only take large square images, which are stored under 'thumbLarge' format. (150x150)
                                    if (!TextUtils.isEmpty(gsonResponse.getFormat()) && gsonResponse.getFormat().equals("thumbLarge")) {
                                        listImage.add(gsonResponse);
                                    }
                                }
                            }
                            if (!listImage.isEmpty()) {
                                showImages(listImage);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Error: ").append(httpStatusCode).append(" - ").append(httpStatusPhrase);
                Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }

            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
        }
    }
}
