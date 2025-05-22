import {
  withInfoPlist,
  withAndroidManifest,
  ConfigPlugin,
} from "expo/config-plugins";

const withPlayerAppConfig: ConfigPlugin = (config) => {
  config = withInfoPlist(config, (config) => {
    // config.modResults["MY_CUSTOM_API_KEY"] = apiKey;
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
    // const mainApplication = AndroidConfig.Manifest.getMainApplicationOrThrow(
    //   config.modResults
    // );

    // AndroidConfig.Manifest.addMetaDataItemToMainApplication(
    //   mainApplication,
    //   "MY_CUSTOM_API_KEY",
    //   apiKey
    // );
    return config;
  });

  return config;
};

export default withPlayerAppConfig;
