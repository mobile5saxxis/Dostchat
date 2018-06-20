package com.dostchat.dost.helpers;

import com.dostchat.dost.models.messages.MessagesModel;

import java.util.Comparator;

/**
 * Created by Abderrahim El imame on 6/24/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class StringDateComparatorMessages implements Comparator<MessagesModel> {

    public int compare(MessagesModel app1, MessagesModel app2) {
        String date1 = app1.getDate();
        String date2 = app2.getDate();
        return date1.compareTo(date2);
    }
    /* try {
            Collections.sort(messagesModelList, new StringDateComparatorMessages());
        } catch (Exception e) {
            AppHelper.LogCat("messages compare " + e.getMessage());
        }*/
}

