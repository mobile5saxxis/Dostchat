package com.dostchat.dost.activities.settings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.dostchat.dost.R;
import com.dostchat.dost.helpers.AppHelper;

public class FAQActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.faq);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        findViewById(R.id.tv_manage).setOnClickListener(this);
        findViewById(R.id.tv_delete).setOnClickListener(this);
        findViewById(R.id.tv_saving).setOnClickListener(this);
        findViewById(R.id.tv_group_chat).setOnClickListener(this);
        findViewById(R.id.tv_adding_contacts).setOnClickListener(this);
        findViewById(R.id.tv_delete_contacts).setOnClickListener(this);
        findViewById(R.id.tv_connection_problem).setOnClickListener(this);
        findViewById(R.id.tv_problem).setOnClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(View view) {
        int titleId;
        String description;

        switch (view.getId()) {
            default:
            case R.id.tv_manage:
                titleId = R.string.managing_your_profile;
                description = "<H3 CLASS='western'><FONT SIZE=4>Profile photo</FONT></H3><OL><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Go to DOSTCHAT &gt; </FONT><FONT SIZE=4><B>Menu Button</B></FONT><FONT SIZE=4>&gt;Settings.</FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in;line-height: 100%'><FONT SIZE=4>Tap your profile photo &gt; Camera icon.</FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>You can choose a photo from your gallery, take a new photo with your camera or remove your current photo.</FONT></P></OL><H3 CLASS='western'><FONT SIZE=4>Name</FONT></H3><OL>	<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Go to DOSTCHAT &gt; </FONT><FONT SIZE=4><B>Menu Button</B></FONT><FONT SIZE=4>&gt; Settings.</FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Tap your profile photo and then tap your name. </FONT>	</P></OL>";
                break;
            case R.id.tv_delete:
                titleId = R.string.deleting_your_account;
                description = "<OL><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Open DOSTCHAT APP.</FONT></P><LI><P STYLE='margin-top: 0.19in;margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the </FONT><FONT SIZE=4><B>Menu Button</B></FONT><FONT SIZE=4>&gt; Settings &gt; Account &gt; Delete my account.</FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Enter your phone number in full international format and tap Delete my account.</FONT></P></OL><H5 CLASS='western'><FONT SIZE=4>Deleting your account will</FONT></H5><UL><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Delete your account from DOSTCHAT APP.</FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT SIZE=4>Erase your message history.</FONT></P></UL>";
                break;
            case R.id.tv_saving:
                titleId = R.string.saving_your_chat_history;
                description = "<OL><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>Open the chat for the individual or group.</FONT></FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap the </FONT></FONT><FONT COLOR='#0000ff'><FONT FACE='Times New Roman, serif'><FONT SIZE=4><U><B>Menu Button</B></U></FONT></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>.</FONT></FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>More</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>.</FONT></FONT></P>\n" +
                        "\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Email chat</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>.</FONT></FONT></P><LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>Choose whether to</FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Attach Media</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4> or not.</FONT></FONT></P>\n" +
                        "</OL>";
                break;
            case R.id.tv_group_chat:
                titleId = R.string.using_group_chats;
                description = "<H3 CLASS='western'><FONT SIZE=4>Creating a group</FONT></H3><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>You maycreate an unlimited number of groups. To create a group:</FONT></FONT></FONT></P><OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Open DOSTCHAT APP and go to the Chats tab.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the </FONT><FONT SIZE=4><B>Menu Button</B></FONT><FONT SIZE=4>\t&gt; New Group.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the names of the desired group participants to add\tthem to the group and then tap the green arrow.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Type in a subject or title. This will be the name of\tthe group that all participants will see. </FONT>\t</P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Add an icon for the group by tapping on the empty photo\tbox. This will show up next to the group in your Chats list.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the green check mark when you are finished.</FONT></P></OL><H3 CLASS='western'><A NAME='add'></A><FONT SIZE=4>Addingparticipants</FONT></H3><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>Youmust be a group admin to add participants to an existing Group Chat.A group can have 256 participants. To add participants:</FONT></FONT></FONT></P><OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Go to the group in DOSTCHAT APP.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the subject of the group.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap Add Participant.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Search for or select contacts to add to the Group Chat.</FONT></P></OL><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><A NAME='invite'></A><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>Theadmin can use the Revoke link option at any time to make the previouslink invalid and create a new link. Anyone with WhatsApp can followan invite link to join this group, so only share it with people youtrust. </FONT></FONT></FONT></P><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>Note:Only use this feature with trusted individuals. It is possible forsomeone to forward the link to other people. If so, those otherpeople can also join the group. And in that case, the group adminwill not be asked to approve them before they join.</FONT></FONT></FONT></P><H3 CLASS='western'><A NAME='remove'></A><FONT SIZE=4>Removingparticipants</FONT></H3><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>Youmust be a group admin to remove participants from an existing GroupChat. To remove participants:</FONT></FONT></FONT></P><OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Go to the group in DOSTCHAT APP.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the subject of the group.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap and hold the participant you wish to remove.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap Remove {participant} in the menu.</FONT></P></OL><H3 CLASS='western'><A NAME='change-subject'></A><A NAME='assign'></A><FONT SIZE=4>Changing the group subject</FONT></H3><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>Any ofthe group's participants can change its subject. To do so:</FONT></FONT></FONT></P><OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Go to the group in DOSTCHAT APP.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the subject of the group.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Tap the Pencil icon to the right of the group icon and\tsubject.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Enter a new subject for the group, then tap OK.</FONT></P></OL><P STYLE='margin-bottom: 0.14in'><BR><BR></P>";
                break;
            case R.id.tv_adding_contacts:
                titleId = R.string.adding_contacts;
                description = "<H3 CLASS='western'><FONT SIZE=4>Adding a new contact</FONT></H3><OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Save a contact's name and phone number in your phone's\taddress book. </FONT>\t</P>\t<UL>\t\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t\t<FONT SIZE=4>If it's a local number: Save the number in the same\t\tformat you would use if you were to call that contact.</FONT></P>\t\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t\t<FONT SIZE=4>If it's a foreign number: Save the number in full\t\tinternational format: </FONT>\t\t</P>\t\t<UL>\t\t\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t\t\t<FONT SIZE=4>+ [Country Code] [Full Phone Number].</FONT></P>\t\t\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t\t\t<FONT SIZE=4>Omit any leading 0's from the phone number.</FONT></P>\t\t</UL>\t</UL></OL><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Deleting contacts</B></FONT></FONT></P><OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Open DOSTCHAT APP\tand go to the </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Chats</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\ttab.</FONT></FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap the contact &gt;\tTap the name at the top of the chat screen.</FONT></FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap the </FONT></FONT><FONT COLOR='#0000ff'><FONT FACE='Times New Roman, serif'><FONT SIZE=4><U><B>Menu\tButton</B></U></FONT></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\t&gt; </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>View\tin address book</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\t&gt; </FONT></FONT><FONT COLOR='#0000ff'><FONT FACE='Times New Roman, serif'><FONT SIZE=4><U><B>Menu\tButton</B></U></FONT></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\t&gt; </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Delete</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>.</FONT></FONT></P></OL><P STYLE='margin-bottom: 0.14in'><BR><BR></P>";
                break;
            case R.id.tv_delete_contacts:
                titleId = R.string.deleting_contacts;
                description = "" +
                        "<OL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Open DOSTCHAT APP\n" +
                        "\tand go to the </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Chats</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\ttab.</FONT></FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap the contact &gt;\tTap the name at the top of the chat screen.</FONT></FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Tap the </FONT></FONT><FONT COLOR='#0000ff'><FONT FACE='Times New Roman, serif'><FONT SIZE=4><U><B>Menu\tButton</B></U></FONT></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\t&gt; </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>View\tin address book</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\t&gt; </FONT></FONT><FONT COLOR='#0000ff'><FONT FACE='Times New Roman, serif'><FONT SIZE=4><U><B>Menu\tButton</B></U></FONT></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>\t&gt; </FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4><B>Delete</B></FONT></FONT><FONT FACE='Times New Roman, serif'><FONT SIZE=4>.</FONT></FONT></P></OL><P STYLE='margin-bottom: 0.14in'><BR><BR></P>";
                break;
            case R.id.tv_connection_problem:
                titleId = R.string.connection_problem;
                description = "<H1 CLASS='western'><FONT SIZE=4>Connection problems</FONT></H1><H3 CLASS='western'><FONT SIZE=4>Troubleshooting</FONT></H3><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=3><FONT SIZE=4>Mostconnection issues can be resolved by doing the following:</FONT></FONT></FONT></P><UL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Restart your phone, or turn it off and on.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Open your phone's Settings &gt; tap Network &amp;\tInternet &gt; toggle to turn Airplane mode on and off.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Open your phone's Settings &gt; tap Network &amp;\tInternet &gt; Data usage &gt; toggle to turn Cellular data on.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Open your phone's Settings &gt; tap Apps &amp;\tnotifications &gt; App info &gt; WhatsApp &gt; Data usage &gt;\ttoggle to turn on Background data.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Open your phone's Settings &gt; Network &amp; Internet\t&gt; Wi-Fi &gt; toggle to turn Wi-Fi off and on.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Try connecting to different Wi-Fi hotspots.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Make sure Wi-Fi stays on during sleep mode.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Reboot your Wi-Fi router.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Contact your mobile provider and make sure your APN\tsettings are configured correctly.</FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT SIZE=4>Upgrade your Android operating system to the latest\tversion available for your phone.</FONT></P></UL>";
                break;
            case R.id.tv_problem:
                titleId = R.string.problem_sending_or_receiving_messages;
                description = "<P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>The most commonreason why you can't send or receive DOSTCHAT APP messages is a badInternet connection. If you're sure your phone is connected to theInternet, there are a few reasons why DOSTCHAT APP messages aren'tgoing through:</FONT></FONT></P><UL>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>Your phone needs to\tbe restarted or turned off and on.</FONT></FONT></P>\t<LI><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'>\t<FONT FACE='Times New Roman, serif'><FONT SIZE=4>The contact you're\tmessaging has blocked your number.</FONT></FONT></P></UL><P STYLE='margin-left: 0.5in; margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><FONT FACE='Times New Roman, serif'><FONT SIZE=4>.</FONT></FONT></P><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><BR><BR></P><P STYLE='margin-top: 0.19in; margin-bottom: 0.19in; line-height: 100%'><BR><BR></P>";
                break;
        }

        Intent intent = new Intent(this, AnswerActivity.class);
        intent.putExtra(AnswerActivity.TITLE, titleId);
        intent.putExtra(AnswerActivity.DESCRIPTION, description);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
