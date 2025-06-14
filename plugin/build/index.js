"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const config_plugins_1 = require("expo/config-plugins");
const { getMainApplicationOrThrow } = config_plugins_1.AndroidConfig.Manifest;
const withPlayerAppConfig = (config) => {
    config = (0, config_plugins_1.withInfoPlist)(config, (config) => {
        // webrtc-signal-scheduler.tlivesource.com域名http请求
        const ats = config.modResults.NSAppTransportSecurity ?? {};
        const exceptionDomains = ats.NSExceptionDomains ?? {};
        exceptionDomains["webrtc-signal-scheduler.tlivesource.com"] = {
            NSExceptionAllowsInsecureHTTPLoads: true,
            NSIncludesSubdomains: true,
        };
        ats.NSExceptionDomains = exceptionDomains;
        config.modResults.NSAppTransportSecurity = ats;
        // 画中画权限
        const bgModes = config.modResults.UIBackgroundModes;
        const requiredModes = ["audio", "picture-in-picture"];
        if (Array.isArray(bgModes)) {
            for (const mode of requiredModes) {
                if (!bgModes.includes(mode)) {
                    bgModes.push(mode);
                }
            }
            config.modResults.UIBackgroundModes = bgModes;
        }
        else {
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
    config = (0, config_plugins_1.withAndroidManifest)(config, (config) => {
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
            const alreadyExists = manifest.manifest["uses-permission"].some((item) => {
                return item.$["android:name"] === permission;
            });
            if (!alreadyExists) {
                manifest.manifest["uses-permission"].push({
                    $: { "android:name": permission },
                });
            }
        }
        // 尝试添加 android:networkSecurityConfig
        // ⚠️ 注意：此属性不会自动合并进宿主 app 的 AndroidManifest.xml，最终仍需宿主 app 手动配置
        if (!mainApplication.$["android:networkSecurityConfig"]) {
            mainApplication.$["android:networkSecurityConfig"] =
                "@xml/network_security_config";
        }
        mainApplication.$["android:usesCleartextTraffic"] = "true";
        // 添加 PipPlayerActivity,画中画
        const activityExists = mainApplication.activity?.some((activity) => activity.$["android:name"] === "expo.modules.txplayer.PipPlayerActivity");
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
                        "android:configChanges": "orientation|keyboardHidden|screenSize|smallestScreenSize|screenLayout",
                    },
                },
            ];
        }
        return config;
    });
    return config;
};
exports.default = withPlayerAppConfig;
