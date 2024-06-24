package com.osaka.sdk.xiaomi;

import android.content.Context;

public class AdjustXiaomiReferrer {

   static boolean shouldReadXiaomiReferrer = true;

   public static void readXiaomiReferrer(Context context) {
      shouldReadXiaomiReferrer = true;
   }

   public static void doNotReadXiaomiReferrer() {
      shouldReadXiaomiReferrer = false;
   }
}
