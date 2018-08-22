package cordova.plugin.tapjoy;

import android.util.Log;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;
import java.util.Hashtable;

/**
 * This class echoes a string called from JavaScript.
 */
public class PluginTapjoy extends CordovaPlugin implements TJPlacementListener {
  public static final String TAG = "PluginTapjoy";
  private Hashtable<String, Object> placements = new Hashtable<String, Object>();

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("setup")) {
      Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
      connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, Boolean.valueOf(args.getString(0)));

      String userId = args.getString(1);
      String appKey = args.getString(2);
      Log.i(TAG, "App setting info - App Key: " + appKey + " User Id: " + userId);

      this.setup(connectFlags, userId, appKey, callbackContext);
      return true;
    }

    if (action.equals("createPlacement")) {
      String placementName = args.getString(0);
      Log.i(TAG, "placementName: " + placementName);

      PluginTapjoy.this.createPlacement(placementName, callbackContext);
      return true;
    }

    if (action.equals("requestContent")) {
      String placementName = args.getString(0);
      Log.i(TAG, "requestContent: " + placementName);

      PluginTapjoy.this.requestContent(placementName, callbackContext);
      return true;
    }

    if (action.equals("showContent")) {
      String placementName = args.getString(0);
      Log.i(TAG, "showContent: " + placementName);

      PluginTapjoy.this.showContent(placementName, callbackContext);
      return true;
    }

    if (action.equals("setUserID")) {
      String userId = args.getString(0);
      Log.i(TAG, "setUserID: " + userId);

      PluginTapjoy.this.setUserID(userId, callbackContext);
      return true;
    }

    if (action.equals("setUserLevel")) {
      Integer userLevel = args.getInt(0);
      Log.i(TAG, "setUserID: " + userLevel);

      PluginTapjoy.this.setUserLevel(userLevel, callbackContext);
      return true;
    }

    if (action.equals("setUserCohortVariable")) {
      Integer cohorIndex = args.getInt(0);
      String cohortValue = args.getString(1);

      Log.i(TAG, "setUserCohortVariable - index: " + cohorIndex + " value: " + cohortValue);

      PluginTapjoy.this.setUserCohortVariable(cohorIndex, cohortValue, callbackContext);
      return true;
    }

    return false;
  }

  private void setup(Hashtable<String, Object> debugMode, String userId, String appKey, CallbackContext callbackContext) {
    if (appKey != null && appKey.length() > 0) {
      Tapjoy.connect(cordova.getActivity().getApplicationContext(), appKey, debugMode, new TJConnectListener() {
        @Override
        public void onConnectSuccess() {
          Log.i(TAG, "onConnectSuccess: Connected to Tapjoy");
          callbackContext.success("Connected to Tapjoy");
        }

        @Override
        public void onConnectFailure() {
          Log.i(TAG, "onConnectFailure: Can not connected to Tapjoy");
          callbackContext.error("Can not connected to Tapjoy.");
        }
      });
    } else {
      callbackContext.error("Can not connected to Tapjoy. AppKey not valid.");
    }
  }

  private void createPlacement(String placementName, CallbackContext callbackContext) {
    TJPlacement placement = new TJPlacement(cordova.getActivity(), placementName, this);

    if (Tapjoy.isConnected()) {
      placements.put(placementName, placement);
      callbackContext.success("Create placement " + placementName + "successfully");
    } else {
      Log.d(TAG, "Tapjoy SDK must finish connecting before requesting content.");
      callbackContext.error("Can not create placement for " + placementName);
    }
  }

  public void requestContent(String placementName, CallbackContext callbackContext) {
    Log.i(TAG, "requestContent for: " + placementName);

    TJPlacement selectedPlacement = (TJPlacement) placements.get(placementName);
    if (selectedPlacement.isContentAvailable()) {
      callbackContext.success("Content of " + placementName + " placement is ready for show.");
      return;
    }

    selectedPlacement = Tapjoy.getPlacement(placementName, new TJPlacementListener() {
      @Override
      public void onRequestSuccess(TJPlacement placement) {
        Log.i(TAG, "onRequestSuccess for placement " + placement.getName());

        if (!placement.isContentAvailable()) {
          Log.i(TAG,"No content available for placement " + placement.getName());
          callbackContext.error("Can not request content from Tapjoy of " + placementName);
        }
      }

      @Override
      public void onRequestFailure(TJPlacement placement, TJError error) {
        Log.i(TAG, "onRequestFailure for placement " + placement.getName() + " -- error: " + error.message);
        callbackContext.error("Can not request content of " + placementName);
      }

      @Override
      public void onContentReady(TJPlacement placement) {
        Log.i(TAG, "onContentReady for placement " + placement.getName());
        callbackContext.success("Content of " + placementName + " placement is ready for show.");
      }

      @Override
      public void onContentShow(TJPlacement placement) {
        Log.i(TAG, "onContentShow for placement " + placement.getName());
      }

      @Override
      public void onContentDismiss(TJPlacement placement) {
        Log.i(TAG, "onContentDismiss for placement " + placement.getName());
      }

      @Override
      public void onPurchaseRequest(TJPlacement placement, TJActionRequest request, String productId) {
        request.completed();
      }

      @Override
      public void onRewardRequest(TJPlacement placement, TJActionRequest request, String itemId, int quantity) {
        request.completed();
      }
    });

    selectedPlacement.requestContent();
  }

  public void showContent(String placementName, CallbackContext callbackContext) {
    Log.i(TAG, "showContent for: " + placementName);
    TJPlacement selectedPlacement = (TJPlacement) placements.get(placementName);

    selectedPlacement = Tapjoy.getPlacement(placementName, new TJPlacementListener() {
      @Override
      public void onRequestSuccess(TJPlacement placement) {
        Log.i(TAG, "onRequestSuccess for placement " + placement.getName());

        if (!placement.isContentAvailable()) {
            Log.i(TAG,"No content available for placement " + placement.getName());
            callbackContext.error("No content available for placement " + placement.getName());
        }
      }

      @Override
      public void onRequestFailure(TJPlacement placement, TJError error) {
        Log.i(TAG, "onRequestFailure for placement " + placement.getName() + " -- error: " + error.message);
        callbackContext.error("onRequestFailure for placement " + placement.getName() + " -- error: " + error.message);
      }

      @Override
      public void onContentReady(TJPlacement placement) {
        Log.i(TAG, "onContentReady for placement " + placement.getName());
        callbackContext.success("onContentReady for placement " + placement.getName());
        placement.showContent();
      }

      @Override
      public void onContentShow(TJPlacement placement) {
        Log.i(TAG, "onContentShow for placement " + placement.getName());
      }

      @Override
      public void onContentDismiss(TJPlacement placement) {
        Log.i(TAG, "onContentDismiss for placement " + placement.getName());
      }

      @Override
      public void onPurchaseRequest(TJPlacement placement, TJActionRequest request, String productId) {
        request.completed();
      }

      @Override
      public void onRewardRequest(TJPlacement placement, TJActionRequest request, String itemId, int quantity) {
        request.completed();
      }
    });

    selectedPlacement.requestContent();
  }

  public void setUserID(String userId, CallbackContext callbackContext) {
    if (userId != null) {
      Tapjoy.setUserID(userId);
      Log.i(TAG, "Set user ID " + userId + "successed");
      callbackContext.success("Set user ID " + userId + "successed");
    } else {
      Log.i(TAG, "Set user ID " + userId + "failed");
      callbackContext.error("Set user ID " + userId + "failed");
    }
  }

  public void setUserLevel(Integer userLevel, CallbackContext callbackContext) {
    if (userLevel > 0) {
      Tapjoy.setUserLevel(userLevel);
      Log.i(TAG, "Set User Level " + userLevel + "successed");
      callbackContext.success("Set User Level " + userLevel + "successed");
    } else {
      Log.i(TAG, "Set User Level " + userLevel + "failed");
      callbackContext.error("Set User Level " + userLevel + "failed");
    }
  }

  public void setUserCohortVariable(Integer cohortIndex, String cohortValue, CallbackContext callbackContext) {
    if (cohortIndex >= 1 && cohortIndex <= 5 && cohortValue != null) {
      Tapjoy.setUserCohortVariable(cohortIndex, cohortValue);
      Log.i(TAG, "Set User Cohort Variable - Index: " + cohortIndex + " - Value: " + cohortValue + " successed");
      callbackContext.success("Set User Cohort Variable - Index: " + cohortIndex + " - Value: " + cohortValue + " successed");
    } else {
      Log.i(TAG, "Set User Cohort Variable failed");
      callbackContext.error("Set User Cohort Variable failed");
    }
  }

  @Override
  public void onRequestSuccess(TJPlacement tjPlacement) {
    Log.i(TAG, "Tapjoy on request success, contentAvailable: " + tjPlacement.isContentAvailable());
  }

  @Override
  public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
    Log.i(TAG, "Tapjoy send event " + tjPlacement.getName() + " failed with error: " + tjError.message);
  }

  @Override
  public void onContentReady(TJPlacement tjPlacement) {
    Log.i(TAG, "Tapjoy on request success, onContentReady : " + tjPlacement.getName());
  }

  @Override
  public void onContentShow(TJPlacement tjPlacement) {
    Log.i(TAG, "Tapjoy onContentShow for: " + tjPlacement.getName());
  }

  @Override
  public void onContentDismiss(TJPlacement tjPlacement) {
    Log.i(TAG, "Tapjoy direct play content did disappear");
  }

  @Override
  public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String productId) {
    String message = "onPurchaseRequest -- placement " + tjPlacement.getName() + " -- product_id " + productId + ", token: " + tjActionRequest.getToken() + ", request id: " + tjActionRequest.getRequestId();
    Log.i(TAG, message);

    tjActionRequest.completed();
  }

  @Override
  public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String itemId, int quantity) {
    String message = "onRewardRequest -- placement " + tjPlacement.getName() + " item_id: " + itemId + ", quantity: " + quantity + ", token: " + tjActionRequest.getToken() + ", request id: " + tjActionRequest.getRequestId();
    Log.i(TAG, message + tjPlacement.getName());

    tjActionRequest.completed();
  }
}
