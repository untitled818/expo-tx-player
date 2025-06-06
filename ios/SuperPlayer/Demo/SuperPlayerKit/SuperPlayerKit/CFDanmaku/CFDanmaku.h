//
//  CFDanmaku.h
//  31- CFDanmakuDemo
//
//  Created by 于 传峰 on 15/7/9.
//  Copyright (c) 2015年 于 传峰. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef enum : NSInteger { CFDanmakuPositionNone = 0, CFDanmakuPositionCenterTop, CFDanmakuPositionCenterBottom } CFDanmakuPosition;

@interface CFDanmaku : NSObject

/// Timestamp corresponding to the video
/// 对应视频的时间戳
@property(nonatomic, assign) NSTimeInterval timePoint;
/// Barrage content
/// 弹幕内容
@property(nonatomic, copy) NSAttributedString* contentStr;
/// Barrage type (if not set, it just scrolls from right to left by default)
/// 弹幕类型(如果不设置 默认情况下只是从右到左滚动)
@property(nonatomic, assign) CFDanmakuPosition position;

/// 弹幕文本颜色
@property(nonatomic, strong) UIColor *textColor;

/// 标识是否是自己发的队列
@property (nonatomic, assign) BOOL isSelf;

@end
