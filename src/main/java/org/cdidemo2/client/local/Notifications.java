package org.cdidemo2.client.local;

/**
 * @author Mike Brock
 */
public class Notifications {
  public static native void initFocusTracking()/*-{
    if (!$wnd.isWindowFocused) {
      $wnd.onblur = function() {
        $wnd.isWindowFocused = "0";
      };
      $wnd.onfocus = function() {
        $wnd.isWindowFocused = "1";
      };

      $wnd.isWindowFocused = "1";
    }
  }-*/;

  public static native boolean hasFocus()/*-{
    return $wnd.isWindowFocused == "1";
  }-*/;

  public static native void ensurePermissions(Runnable runnable)/*-{
    if ($wnd.webkitNotifications) {
      $wnd.webkitNotifications.requestPermission(function() {
        console.log("notifications state: " +$wnd.webkitNotifications.checkPermission() );
        runnable.@java.lang.Runnable::run()();  });
    }
    else {
      runnable.@java.lang.Runnable::run()();
    }
  }-*/;

  public static native void notify(String title, String message)/*-{
     if ($wnd.webkitNotifications) {
       if ($wnd.webkitNotifications.checkPermission() == 0) {
         console.log("notifying: " + message);
          $wnd.webkitNotifications.createNotification("<i class='icon-search icon-comment'>", title, message).show();
       }
     }
  }-*/;
}
