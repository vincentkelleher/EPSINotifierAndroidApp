package fr.epsi.epsinotifier.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import cz.msebera.android.httpclient.Header;
import fr.epsi.epsinotifier.app.R;
import fr.epsi.epsinotifier.entity.Cours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class EPSIIntentService extends IntentService {

    private AsyncHttpClient asyncHttpClient = new SyncHttpClient();

    public EPSIIntentService() {
        super("EPSIIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        asyncHttpClient.get(this, "http://epsi-planning.herokuapp.com/cours/prochain", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Cours cours = new Cours();

                try {
                    JSONObject coursJson = new JSONObject(new String(responseBody, "UTF-8"));
                    JSONObject dataJson = coursJson.getJSONArray("data").getJSONObject(0);

                    cours.setMatiere(dataJson.getString("matiere"));
                    cours.setProf(dataJson.getString("prof"));
                    cours.setSalle(dataJson.getString("salle"));

                    String horaireDebut = dataJson.getString("horaire_debut");
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
                    cours.setHoraireDebut(formatter.parseDateTime(horaireDebut));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                sendNotification(cours);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("JSON", error.getMessage());
            }
        });
    }

    private void sendNotification(Cours cours) {
        int notificationId = 1;

        DateTimeFormatter outputDateFormatter = DateTimeFormat.forPattern("HH:mm");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(cours.getMatiere())
                .setContentText(outputDateFormatter.print(cours.getHoraireDebut()) + " - " + cours.getSalle());

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
