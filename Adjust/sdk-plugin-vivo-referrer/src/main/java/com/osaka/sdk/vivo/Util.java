package com.osaka.sdk.vivo;

import android.content.Context;

import com.osaka.sdk.ILogger;
import com.osaka.sdk.ReferrerDetails;

public class Util {
   public synchronized static ReferrerDetails getVivoInstallReferrerDetails(Context context, ILogger logger) {
      if (!AdjustVivoReferrer.shouldReadVivoReferrer) {
         return null;
      }

      return VivoReferrerClient.getReferrer(context, logger);
   }
}
