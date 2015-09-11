package com.money.manager.ex.investment;

import android.content.Context;

/**
 * Factory for security price updater.
 * Set here when changing the updater.
 */
public class SecurityPriceUpdaterFactory {
    public static ISecurityPriceUpdater getUpdaterInstance(Context context, IPriceUpdaterFeedback feedback) {
        ISecurityPriceUpdater updater;

//        updater = new YahooSecurityPriceUpdater(context, feedback);

        updater = new YqlSecurityPriceUpdater(context, feedback);

        return updater;
    }
}