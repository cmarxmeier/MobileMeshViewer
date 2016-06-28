package freifunk.bremen.de.mobilemeshviewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.inject.Inject;

import roboguice.activity.RoboAppCompatActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_about_ffhb)
public class AboutFFHB extends RoboAppCompatActivity {

    @InjectView(R.id.toolbar)
    private Toolbar toolbar;
    @InjectView(R.id.contact_button_mail)
    private Button buttonMail;
    @InjectView(R.id.contact_button_irc)
    private Button buttonChat;
    @InjectView(R.id.contact_button_twitter)
    private Button buttonTwitter;
    @InjectView(R.id.contact_button_facebook)
    private Button buttonFacebook;
    @InjectView(R.id.contact_button_website)
    private Button buttonWebsite;
    @Inject
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(context.getString(R.string.title_activity_about_ffhb));

        buttonMail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: " + context.getString(R.string.contact_e_mail)));
                startActivity(Intent.createChooser(emailIntent, context.getString(R.string.contact_e_mail_message)));
            }
        });

        buttonChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent webIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.contact_chat)));
                startActivity(Intent.createChooser(webIntent, context.getString(R.string.contact_chat_message)));
            }
        });

        buttonTwitter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent webIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.contact_twitter)));
                startActivity(Intent.createChooser(webIntent, context.getString(R.string.contact_twitter_message)));
            }
        });

        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent webIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.contact_facebook)));
                startActivity(Intent.createChooser(webIntent, context.getString(R.string.contact_facebook_message)));
            }
        });

        buttonWebsite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent webIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.contact_website)));
                startActivity(Intent.createChooser(webIntent, context.getString(R.string.contact_website_message)));
            }
        });

    }

}