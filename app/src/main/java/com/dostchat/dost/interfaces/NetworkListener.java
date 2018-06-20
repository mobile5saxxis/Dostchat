package com.dostchat.dost.interfaces;

/**
 * Created by Abderrahim El imame on 9/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */


public interface NetworkListener {
    void onNetworkConnectionChanged(boolean isConnecting, boolean isConnected);
}
