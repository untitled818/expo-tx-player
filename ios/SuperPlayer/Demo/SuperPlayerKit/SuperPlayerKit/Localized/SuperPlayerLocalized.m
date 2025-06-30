//
//  SuperPlayerLocalized.m
//  Pods
//
//  Copyright © 2022年 Tencent. All rights reserved.
//

#import "SuperPlayerLocalized.h"

NSString *superPlayerLocalizeFromTable(NSString *key, NSString *table) {
    NSString *resourcePath = [[NSBundle mainBundle] pathForResource:@"SuperPlayerKitBundle" ofType:@"bundle"];
//    NSString *resourcePath = [[NSBundle mainBundle] pathForResource:@"SuperPlayerLocalized" ofType:@"bundle"];
    NSLog(@"[Debug] Bundle 路径：%@", resourcePath);
    NSBundle *bundle = [NSBundle bundleWithPath:resourcePath];
    NSLog(@"[Debug] bundle = %@", bundle);
    NSLog(@"[Debug] bundle localizations = %@", [bundle localizations]);
    NSLog(@"[Debug] preferred localizations = %@", [bundle preferredLocalizations]);
    return [bundle localizedStringForKey:key value:@"" table:table];
}

NSString *const SuperPlayer_Localize_TableName = @"SuperPlayerLocalized";
NSString *      superPlayerLocalized(NSString *key) {
    return superPlayerLocalizeFromTable(key, SuperPlayer_Localize_TableName);
}
