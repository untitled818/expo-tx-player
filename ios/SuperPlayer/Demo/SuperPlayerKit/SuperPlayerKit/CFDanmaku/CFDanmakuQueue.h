//
//  CFDanmakuQueue.h
//  Pods
//
//  Created by fin on 28.5.2025.
//

#import <Foundation/Foundation.h>
@class CFDanmakuInfo;

@interface CFDanmakuQueue : NSObject
@property (nonatomic, assign, readonly) NSInteger count;
- (void)enqueue:(CFDanmakuInfo *)danmaku;
- (CFDanmakuInfo *)dequeue;
- (BOOL)isEmpty;
- (CFDanmakuInfo *)peek;

// 清空队列
- (void)clear;
@end


