//
//  SuperPlayerLocalized.m
//  Pods
//
//  Copyright © 2022年 Tencent. All rights reserved.
//

#import "SuperPlayerLocalized.h"

//NSString *superPlayerLocalizeFromTable(NSString *key, NSString *table) {
//    NSString *resourcePath = [[NSBundle mainBundle] pathForResource:@"SuperPlayerKitBundle" ofType:@"bundle"];
////    NSString *resourcePath = [[NSBundle mainBundle] pathForResource:@"SuperPlayerLocalized" ofType:@"bundle"];
//    NSLog(@"[Debug] Bundle 路径：%@", resourcePath);
//    NSBundle *bundle = [NSBundle bundleWithPath:resourcePath];
//    NSLog(@"[Debug] bundle = %@", bundle);
//    NSLog(@"[Debug] bundle localizations = %@", [bundle localizations]);
//    NSLog(@"[Debug] preferred localizations = %@", [bundle preferredLocalizations]);
//    return [bundle localizedStringForKey:key value:@"" table:table];
//}

NSString *superPlayerLocalizeFromTable(NSString *key, NSString *table) {
    NSString *bundlePath = [[NSBundle mainBundle] pathForResource:@"SuperPlayerKitBundle" ofType:@"bundle"];
    NSBundle *bundle = [NSBundle bundleWithPath:bundlePath];

    if (!bundle) {
        NSLog(@"⚠️ 没找到 SuperPlayerKitBundle.bundle，fallback 到 mainBundle");
        bundle = [NSBundle mainBundle];
    }

    NSString *language = [NSLocale preferredLanguages].firstObject;
    NSLog(@"当前系统语言为: %@", language);

    NSString *langFolder = nil;
    if ([language hasPrefix:@"zh-Hans"]) {
        langFolder = @"zh-Hans";
    } else if ([language hasPrefix:@"zh-Hant"]) {
        langFolder = @"zh-Hant";
    } else if ([language hasPrefix:@"en"]) {
        langFolder = @"en";
    }

    if (langFolder) {
        NSString *langPath = [bundle pathForResource:langFolder ofType:@"lproj"];
        if (langPath) {
            bundle = [NSBundle bundleWithPath:langPath];
            NSLog(@"✅ 加载语言资源路径: %@", langPath);
        } else {
            NSLog(@"⚠️ 没找到 %@.lproj，仍使用默认 bundle", langFolder);
        }
    }

    NSString *result = [bundle localizedStringForKey:key value:@"[未找到]" table:table];
    return result;
}

NSString *const SuperPlayer_Localize_TableName = @"SuperPlayerLocalized";
NSString *      superPlayerLocalized(NSString *key) {
    return superPlayerLocalizeFromTable(key, SuperPlayer_Localize_TableName);
}
