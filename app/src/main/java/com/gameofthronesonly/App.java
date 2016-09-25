package com.gameofthronesonly;

import com.gameofthronesonly.constants.Consts;

/**
 * Created by NilayS on 7/6/2016.
 */
public class App extends CoreApp {

    @Override
    public void onCreate() {
        super.onCreate();
        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
    }
}