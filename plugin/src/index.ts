import {
  withInfoPlist,
  withAndroidManifest,
  ConfigPlugin,
  AndroidConfig,
} from "expo/config-plugins";

const { getMainApplicationOrThrow } = AndroidConfig.Manifest;

const withPlayerAppConfig: ConfigPlugin = (config) => {
  config = withInfoPlist(config, (config) => {
    // webrtc-signal-scheduler.tlivesource.com域名http请求
    const ats =
      (config.modResults.NSAppTransportSecurity as Record<string, any>) ?? {};
    const exceptionDomains =
      (ats.NSExceptionDomains as Record<string, any>) ?? {};
    exceptionDomains["webrtc-signal-scheduler.tlivesource.com"] = {
      NSExceptionAllowsInsecureHTTPLoads: true,
      NSIncludesSubdomains: true,
    };
    ats.NSExceptionDomains = exceptionDomains;
    config.modResults.NSAppTransportSecurity = ats;

    // 画中画权限
    const bgModes = config.modResults.UIBackgroundModes as string[] | undefined;
    const requiredModes = ["audio", "picture-in-picture"];
    if (Array.isArray(bgModes)) {
      for (const mode of requiredModes) {
        if (!bgModes.includes(mode)) {
          bgModes.push(mode);
        }
      }
      config.modResults.UIBackgroundModes = bgModes;
    } else {
      config.modResults.UIBackgroundModes = requiredModes;
    }

    // 支持横屏方向
    config.modResults.UIRequiresFullScreen = true;
    config.modResults.UISupportedInterfaceOrientations = [
      "UIInterfaceOrientationPortrait",
      "UIInterfaceOrientationLandscapeLeft",
      "UIInterfaceOrientationLandscapeRight",
    ];
    config.modResults["UISupportedInterfaceOrientations~ipad"] = [
      "UIInterfaceOrientationPortrait",
      "UIInterfaceOrientationPortraitUpsideDown",
      "UIInterfaceOrientationLandscapeLeft",
      "UIInterfaceOrientationLandscapeRight",
    ];

    return config;
  });

  config = withAndroidManifest(config, (config) => {
    const manifest = config.modResults;
    const mainApplication = getMainApplicationOrThrow(manifest);

    const permissions = [
      "android.permission.INTERNET",
      "android.permission.ACCESS_NETWORK_STATE",
      "android.permission.ACCESS_WIFI_STATE",
      "android.permission.SYSTEM_ALERT_WINDOW",
      "android.permission.WRITE_EXTERNAL_STORAGE",
      "android.permission.READ_EXTERNAL_STORAGE",
    ];

    manifest.manifest["uses-permission"] ??= [];

    for (const permission of permissions) {
      const alreadyExists = manifest.manifest["uses-permission"].some(
        (item) => {
          return item.$["android:name"] === permission;
        }
      );

      if (!alreadyExists) {
        manifest.manifest["uses-permission"].push({
          $: { "android:name": permission },
        });
      }
    }

    // 尝试添加 android:networkSecurityConfig
    // ⚠️ 注意：此属性不会自动合并进宿主 app 的 AndroidManifest.xml，最终仍需宿主 app 手动配置
    // if (!mainApplication.$["android:networkSecurityConfig"]) {
    //   mainApplication.$["android:networkSecurityConfig"] =
    //     "@xml/network_security_config";
    // }

    mainApplication.$["android:usesCleartextTraffic"] = "true";

    // 添加 PipPlayerActivity,画中画
    const activityExists = mainApplication.activity?.some(
      (activity) =>
        activity.$["android:name"] === "expo.modules.txplayer.PipPlayerActivity"
    );
    if (!activityExists) {
      mainApplication.activity = [
        ...(mainApplication.activity ?? []),
        {
          $: {
            "android:name": "expo.modules.txplayer.PipPlayerActivity",
            "android:resizeableActivity": "true",
            "android:supportsPictureInPicture": "true",
            "android:documentLaunchMode": "intoExisting",
            "android:excludeFromRecents": "true",
            "android:configChanges":
              "orientation|keyboardHidden|screenSize|smallestScreenSize|screenLayout",
          },
        },
      ];
    }

    return config;
  });

  return config;
};

export default withPlayerAppConfig;
