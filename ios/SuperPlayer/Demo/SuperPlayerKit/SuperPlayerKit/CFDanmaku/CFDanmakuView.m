//
//  CFDanmakuView.m
//  31- CFDanmakuDemo
//
//  Created by 于 传峰 on 15/7/9.
//  Copyright (c) 2015年 于 传峰. All rights reserved.
//

#import "CFDanmakuView.h"

//#import "AppLocalized.h"
#import "SuperPlayerLocalized.h"
#import "CFDanmakuInfo.h"
#import "CFDanmakuQueue.h"

#define X(view)       view.frame.origin.x
#define Y(view)       view.frame.origin.y
#define Width(view)   view.frame.size.width
#define Height(view)  view.frame.size.height
#define Left(view)    X(view)
#define Right(view)   (X(view) + Width(view))
#define Top(view)     Y(view)
#define Bottom(view)  (Y(view) + Height(view))
#define CenterX(view) (Left(view) + Right(view)) / 2
#define CenterY(view) (Top(view) + Bottom(view)) / 2

@interface CFDanmakuView () {
    NSTimer* _timer;
}
@property(nonatomic, strong) NSMutableArray* danmakus;
@property(nonatomic, strong) NSMutableArray* currentDanmakus;
@property(nonatomic, strong) NSMutableArray* subDanmakuInfos;

@property(nonatomic, strong) NSMutableDictionary* linesDict;
@property(nonatomic, strong) NSMutableDictionary* centerTopLinesDict;
@property(nonatomic, strong) NSMutableDictionary* centerBottomLinesDict;

@property(nonatomic, strong) CFDanmakuQueue *waitingQueue;
@property(nonatomic, strong) CFDanmakuQueue *selfDanmakuQueue;

@property (nonatomic, assign, readwrite) BOOL danmakuVisible;

//@property(nonatomic, assign) BOOL centerPause;

@end

static NSTimeInterval const timeMargin = 0.5;
@implementation             CFDanmakuView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.danmakuVisible = true;
        self.userInteractionEnabled = NO;
        self.backgroundColor        = [UIColor clearColor];
        
        _linesDict = [NSMutableDictionary dictionary];
        _waitingQueue = [[CFDanmakuQueue alloc] init];
        _selfDanmakuQueue = [[CFDanmakuQueue alloc] init];
    }
    return self;
}

#pragma mark - lazy
- (NSMutableArray*)subDanmakuInfos {
    if (!_subDanmakuInfos) {
        _subDanmakuInfos = [[NSMutableArray alloc] init];
    }
    return _subDanmakuInfos;
}

- (NSMutableDictionary*)linesDict {
    if (!_linesDict) {
        _linesDict = [[NSMutableDictionary alloc] init];
    }
    return _linesDict;
}

- (NSMutableDictionary*)centerBottomLinesDict {
    if (!_centerBottomLinesDict) {
        _centerBottomLinesDict = [[NSMutableDictionary alloc] init];
    }
    return _centerBottomLinesDict;
}

- (NSMutableDictionary*)centerTopLinesDict {
    if (!_centerTopLinesDict) {
        _centerTopLinesDict = [[NSMutableDictionary alloc] init];
    }
    return _centerTopLinesDict;
}

- (NSMutableArray*)currentDanmakus {
    if (!_currentDanmakus) {
        _currentDanmakus = [NSMutableArray array];
    }
    return _currentDanmakus;
}

#pragma mark - perpare
- (void)prepareDanmakus:(NSArray*)danmakus {
    self.danmakus = [[danmakus sortedArrayUsingComparator:^NSComparisonResult(CFDanmaku* obj1, CFDanmaku* obj2) {
        if (obj1.timePoint > obj2.timePoint) {
            return NSOrderedDescending;
        }
        return NSOrderedAscending;
    }] mutableCopy];
}

- (void)getCurrentTime {
    //    NSLog(@"getCurrentTime---------");

    if ([self.delegate danmakuViewIsBuffering:self]) return;

    [self.subDanmakuInfos enumerateObjectsUsingBlock:^(CFDanmakuInfo* obj, NSUInteger idx, BOOL* stop) {
        NSTimeInterval leftTime = obj.leftTime;
        leftTime -= timeMargin;
        obj.leftTime = leftTime;
    }];

    [self.currentDanmakus removeAllObjects];
    NSTimeInterval timeInterval = [self.delegate danmakuViewGetPlayTime:self];
    NSString*      timeStr      = [NSString stringWithFormat:@"%0.1f", timeInterval];
    timeInterval                = timeStr.floatValue;

    [self.danmakus enumerateObjectsUsingBlock:^(CFDanmaku* obj, NSUInteger idx, BOOL* stop) {
        if (obj.timePoint >= timeInterval && obj.timePoint < timeInterval + timeMargin) {
            [self.currentDanmakus addObject:obj];
            //            NSLog(@"%f----%f--%zd", timeInterval, obj.timePoint, idx);
        } else if (obj.timePoint > timeInterval) {
            *stop = YES;
        }
    }];

    if (self.currentDanmakus.count > 0) {
        for (CFDanmaku* danmaku in self.currentDanmakus) {
            [self playDanmaku:danmaku];
        }
    }
}


- (void)playDanmaku:(CFDanmaku*)danmaku {
    UILabel* playerLabel       = [[UILabel alloc] init];
    playerLabel.attributedText = danmaku.contentStr;
    [playerLabel sizeToFit];

    playerLabel.backgroundColor = [UIColor clearColor];
    NSLog(@"准备播放弹幕：%@, label size: %@", danmaku.contentStr.string, NSStringFromCGRect(playerLabel.bounds));
    //    [self addSubview:playerLabel];
    //    self.playingLabel = playerLabel;

    switch (danmaku.position) {
        case CFDanmakuPositionNone:
            [self playFromRightDanmaku:danmaku playerLabel:playerLabel];
            break;

        case CFDanmakuPositionCenterTop:
        case CFDanmakuPositionCenterBottom:
            [self playCenterDanmaku:danmaku playerLabel:playerLabel];
            break;

        default:
            break;
    }
}

#pragma mark - center top \ bottom
- (void)playCenterDanmaku:(CFDanmaku*)danmaku playerLabel:(UILabel*)playerLabel {
    NSAssert(self.centerDuration && self.maxCenterLineCount, superPlayerLocalized(@"SuperPlayerDemo.CFDanmaku.usebarrage"));

    CFDanmakuInfo* newInfo = [[CFDanmakuInfo alloc] init];
    newInfo.playLabel      = playerLabel;
    newInfo.leftTime       = self.centerDuration;
    newInfo.danmaku        = danmaku;

    NSMutableDictionary* centerDict = nil;

    if (danmaku.position == CFDanmakuPositionCenterTop) {
        centerDict = self.centerTopLinesDict;
    } else {
        centerDict = self.centerBottomLinesDict;
    }

    NSInteger valueCount = centerDict.allKeys.count;
    if (valueCount == 0) {
        newInfo.lineCount = 0;
        [self addCenterAnimation:newInfo centerDict:centerDict];
        return;
    }
    for (int i = 0; i < valueCount; i++) {
        CFDanmakuInfo* oldInfo = centerDict[@(i)];
        if (!oldInfo) break;
        if (![oldInfo isKindOfClass:[CFDanmakuInfo class]]) {
            newInfo.lineCount = i;
            [self addCenterAnimation:newInfo centerDict:centerDict];
            break;
        } else if (i == valueCount - 1) {
            if (valueCount < self.maxCenterLineCount) {
                newInfo.lineCount = i + 1;
                [self addCenterAnimation:newInfo centerDict:centerDict];
            } else {
                [self.danmakus removeObject:danmaku];
                [playerLabel removeFromSuperview];
                NSLog(@"%@", superPlayerLocalized(@"SuperPlayerDemo.CFDanmaku.toomanycomments"));
            }
        }
    }
}

- (void)addCenterAnimation:(CFDanmakuInfo*)info centerDict:(NSMutableDictionary*)centerDict {
    UILabel*  label     = info.playLabel;
    NSInteger lineCount = info.lineCount;

    if (info.danmaku.position == CFDanmakuPositionCenterTop) {
        label.frame = CGRectMake((Width(self) - Width(label)) * 0.5, (self.lineHeight + self.lineMargin) * lineCount, Width(label), Height(label));
    } else {
        label.frame = CGRectMake((Width(self) - Width(label)) * 0.5, Height(self) - Height(label) - (self.lineHeight + self.lineMargin) * lineCount, Width(label), Height(label));
    }

    centerDict[@(lineCount)] = info;
    [self.subDanmakuInfos addObject:info];

    [self performCenterAnimationWithDuration:info.leftTime danmakuInfo:info];
}

- (void)performCenterAnimationWithDuration:(NSTimeInterval)duration danmakuInfo:(CFDanmakuInfo*)info {
    UILabel* label = info.playLabel;

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(duration * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self->_isPauseing) return;

        if (info.danmaku.position == CFDanmakuPositionCenterBottom) {
            self.centerBottomLinesDict[@(info.lineCount)] = @(0);
        } else {
            self.centerTopLinesDict[@(info.lineCount)] = @(0);
        }

        [label removeFromSuperview];
        [self.subDanmakuInfos removeObject:info];
    });
}
//- (void)playFromRightDanmaku:(CFDanmaku*)danmaku playerLabel:(UILabel*)playerLabel {
//    CFDanmakuInfo* newInfo = [[CFDanmakuInfo alloc] init];
//    newInfo.playLabel = playerLabel;
//    newInfo.leftTime = self.duration;
//    newInfo.danmaku = danmaku;
//    playerLabel.backgroundColor = [UIColor clearColor];
//
//    CGFloat screenWidth = CGRectGetWidth(self.bounds);
//    CGFloat newWidth = CGRectGetWidth(playerLabel.bounds);
//
//    for (NSInteger i = 0; i < self.maxShowLineCount; i++) {
//        NSMutableArray *infosInLine = self.linesDict[@(i)] ?: [NSMutableArray new];
//        self.linesDict[@(i)] = infosInLine;
//
//        BOOL available = YES;
//        for (CFDanmakuInfo *oldInfo in infosInLine) {
//            CALayer *layer = oldInfo.playLabel.layer.presentationLayer ?: oldInfo.playLabel.layer;
//            CGFloat oldX = layer.frame.origin.x;
//            CGFloat oldWidth = CGRectGetWidth(oldInfo.playLabel.bounds);
//
//            // ✅ 改进后的碰撞检测逻辑：判断是否追尾，保持一定间距即可
//            CGFloat minSpacing = 20; // 你可以调成 10 或 30，看你希望多紧凑
//            CGFloat distanceBetween = oldX + oldWidth - screenWidth;
//
//            if (distanceBetween > -minSpacing) {
//                available = NO;
//                break;
//            }
//        }
//
//        if (available) {
//            newInfo.lineCount = i;
//            [infosInLine addObject:newInfo];
//
//            [self addAnimationToViewWithInfo:newInfo];
//            return;
//        }
//    }
//
//    // 加入等待队列
//    [playerLabel removeFromSuperview];
//    [self.waitingQueue enqueue:danmaku];
//}

#pragma mark - from right

- (void)playFromRightDanmaku:(CFDanmaku*)danmaku playerLabel:(UILabel*)playerLabel {
    
    CFDanmakuInfo* info = [[CFDanmakuInfo alloc] init];
//        info.playLabel = playerLabel;
        info.leftTime = self.duration;
        info.danmaku = danmaku;
    
        if (danmaku.isSelf) {
            [self.selfDanmakuQueue enqueue:info];
        } else {
            // 正常弹幕，插队尾
            [self.waitingQueue enqueue:info];
        }
//        [self.waitingQueue enqueue:info];

        [self tryPlayNextDanmaku];
}

// 统一动画速度，防止 后弹幕 追上 前弹幕，造成弹幕重叠
//- (void)addAnimationToViewWithInfo:(CFDanmakuInfo *)info {
//    UILabel *label = [[UILabel alloc] init];
//    label.attributedText = info.danmaku.contentStr;
//    [label sizeToFit];
//    label.backgroundColor = [UIColor clearColor];
//
//    info.playLabel = label;
//    NSInteger line = info.lineCount;
//    NSLog(@"✅ 播放弹幕：%@", info.danmaku.contentStr.string);
//
//    CGFloat y = (self.lineHeight + self.lineMargin) * line;
//    CGFloat labelWidth = CGRectGetWidth(label.bounds);
//    CGFloat screenWidth = CGRectGetWidth(self.bounds);
//
//    // 起始位置：屏幕右侧外
//    label.frame = CGRectMake(screenWidth, y, labelWidth, CGRectGetHeight(label.bounds));
//
//    [self addSubview:label];
//
//    // ✅ 使用统一速度（例如 80 px/s）
//    CGFloat speed = 80.0;
//    CGFloat totalDistance = screenWidth + labelWidth;
//    CGFloat duration = totalDistance / speed;
//
//    [UIView animateWithDuration:duration delay:0 options:UIViewAnimationOptionCurveLinear animations:^{
//        label.frame = CGRectMake(-labelWidth, y, labelWidth, CGRectGetHeight(label.bounds));
//    } completion:^(BOOL finished) {
//        [label removeFromSuperview];
//        NSMutableArray *infosInLine = self.linesDict[@(line)];
//        [infosInLine removeObject:info];
//        [self tryPlayNextDanmaku];
//    }];
//}



//  动画播放和清理 + 触发队列恢复
- (void)addAnimationToViewWithInfo:(CFDanmakuInfo *)info {
//    UILabel *label = info.playLabel;
    UILabel *label = [[UILabel alloc] init];
        label.attributedText = info.danmaku.contentStr;
        [label sizeToFit];
        label.backgroundColor = [UIColor clearColor];

    info.playLabel = label; // 如果你后面需要用它
    NSInteger line = info.lineCount;
    NSLog(@"✅ 播放弹幕：%@", info.danmaku.contentStr.string);
    CGFloat y = (self.lineHeight + self.lineMargin) * line;
    label.frame = CGRectMake(CGRectGetWidth(self.bounds), y, CGRectGetWidth(label.bounds), CGRectGetHeight(label.bounds));
    
    NSLog(@"弹幕内容: %@, frame: %@, bounds: %@", info.danmaku.contentStr.string,
          NSStringFromCGRect(label.frame),
          NSStringFromCGRect(label.bounds));
    
    NSLog(@"label.attributedText: %@", label.attributedText);

    [self addSubview:label];

    [UIView animateWithDuration:info.leftTime delay:0 options:UIViewAnimationOptionCurveLinear animations:^{
        label.frame = CGRectMake(-CGRectGetWidth(label.bounds), y, CGRectGetWidth(label.bounds), CGRectGetHeight(label.bounds));
    } completion:^(BOOL finished) {
        [label removeFromSuperview];
        NSMutableArray *infosInLine = self.linesDict[@(line)];
        [infosInLine removeObject:info];
        
        // 播放等待队列中弹幕
        [self tryPlayNextDanmaku];
    }];
}

//
- (void)tryPlayNextDanmaku {
    while (![self.waitingQueue isEmpty]) {
        // 取出优先队列
        CFDanmakuInfo *nextInfo = ![self.selfDanmakuQueue isEmpty]
                    ? [self.selfDanmakuQueue peek]
                    : [self.waitingQueue peek];
//        CFDanmakuInfo *nextInfo = [self.waitingQueue peek];
        BOOL didPlay = NO;

        for (NSInteger i = 0; i < self.maxShowLineCount; i++) {
            NSMutableArray *infosInLine = self.linesDict[@(i)] ?: [NSMutableArray new];
            self.linesDict[@(i)] = infosInLine;

            BOOL available = YES;
            for (CFDanmakuInfo *oldInfo in infosInLine) {
                CALayer *layer = oldInfo.playLabel.layer.presentationLayer;
                if (!layer) {
                    NSLog(@"轨道 %ld 不可用：有弹幕还未开始动画（presentationLayer 为 nil）", (long)i);
                    available = NO;
                    break;
                }

                CGFloat oldX = layer.frame.origin.x;
                CGFloat oldWidth = CGRectGetWidth(oldInfo.playLabel.bounds);
                CGFloat screenWidth = CGRectGetWidth(self.bounds);
                CGFloat minSpacing = 20;
                CGFloat distanceBetween = oldX + oldWidth - screenWidth;

                if (distanceBetween > -minSpacing) {
                    available = NO;
                    break;
                }
            }

            if (available) {
                // 可以播放
                nextInfo.lineCount = i;
                NSLog(@"➡️ addAnimationToViewWithInfo 被调用，lineCount = %ld", (long)nextInfo.lineCount);
                [infosInLine addObject:nextInfo];
//                [self.waitingQueue dequeue];
                if (![self.selfDanmakuQueue isEmpty]) {
                    [self.selfDanmakuQueue dequeue];
                } else {
                    [self.waitingQueue dequeue];
                }

                [self addAnimationToViewWithInfo:nextInfo];
                didPlay = YES;
                break; // 播完这一条，再 peek 下一条
            }
        }

        // 如果这一条播放不了，那就也别试后面的，直接跳出
        if (!didPlay) break;
    }
}


// 处理多轨道多弹幕，但是弹幕会消失
//- (void)tryPlayNextDanmaku {
//    if ([self.waitingQueue isEmpty]) return;
//
//    CFDanmakuInfo *nextInfo = [self.waitingQueue peek]; // 注意，是 peek！
//
//    for (NSInteger i = 0; i < self.maxShowLineCount; i++) {
//        NSMutableArray *infosInLine = self.linesDict[@(i)] ?: [NSMutableArray new];
//        self.linesDict[@(i)] = infosInLine;
//
//        BOOL available = YES;
//        for (CFDanmakuInfo *oldInfo in infosInLine) {
//            CALayer *layer = oldInfo.playLabel.layer.presentationLayer ?: oldInfo.playLabel.layer;
//            CGFloat oldX = layer.frame.origin.x;
//            CGFloat oldWidth = CGRectGetWidth(oldInfo.playLabel.bounds);
//            CGFloat screenWidth = CGRectGetWidth(self.bounds);
//            CGFloat minSpacing = 20;
//            CGFloat distanceBetween = oldX + oldWidth - screenWidth;
//            if (distanceBetween > -minSpacing) {
//                available = NO;
//                break;
//            }
//        }
//
//        if (available) {
//            // 找到可用轨道：设置轨道、添加到轨道队列、出队、播放
//            nextInfo.lineCount = i;
//            [infosInLine addObject:nextInfo];
//            [self.waitingQueue dequeue];
//            [self addAnimationToViewWithInfo:nextInfo];
//            return;
//        }
//    }
//
//    // 没有轨道，暂时不处理，等待已有弹幕播完后再触发 tryPlayNextDanmaku
//}

- (void)performAnimationWithDuration:(NSTimeInterval)duration danmakuInfo:(CFDanmakuInfo*)info {
    _isPlaying  = YES;
    _isPauseing = NO;

    UILabel* label    = info.playLabel;
    CGRect   endFrame = CGRectMake(-Width(label), Y(label), Width(label), Height(label));

    [UIView animateWithDuration:duration
                          delay:0
                        options:UIViewAnimationOptionCurveLinear
                     animations:^{
        label.frame = endFrame;
    } completion:^(BOOL finished) {
        [label removeFromSuperview];
        [self.subDanmakuInfos removeObject:info];
        
        // 清理轨道
        [self releaseDanmakuLineWithIndex:info.lineCount];
    }];
}

// 清理轨道
- (void)releaseDanmakuLineWithIndex:(NSInteger)lineIndex {
    if ([self.linesDict objectForKey:@(lineIndex)]) {
        [self.linesDict removeObjectForKey:@(lineIndex)];
        NSLog(@"[Danmaku] ✅ 已释放轨道 %ld", (long)lineIndex);
    }
}

// 检测碰撞
- (BOOL)judgeIsRunintoWithFirstDanmakuInfo:(CFDanmakuInfo*)info behindLabel:(UILabel*)last {
    UILabel* firstLabel = info.playLabel;
    CGFloat  firstSpeed = [self getSpeedFromLabel:firstLabel];
    CGFloat  lastSpeed  = [self getSpeedFromLabel:last];

    //    CGRect firstFrame = info.labelFrame;
    CGFloat firstFrameRight = info.leftTime * firstSpeed;

    if (info.leftTime <= 1) return NO;

    if (Left(last) - firstFrameRight > 10) {
        if (lastSpeed <= firstSpeed) {
            return NO;
        } else {
            CGFloat lastEndLeft = Left(last) - lastSpeed * info.leftTime;
            if (lastEndLeft > 10) {
                return NO;
            }
        }
    }

    return YES;
}

// 计算速度
- (CGFloat)getSpeedFromLabel:(UILabel*)label {
    return (self.bounds.size.width + label.bounds.size.width) / self.duration;
}

#pragma mark - 公共方法

- (BOOL)isPrepared {
    NSAssert(self.duration && self.maxShowLineCount && self.lineHeight, superPlayerLocalized(@"SuperPlayerDemo.CFDanmaku.mustsettingbarrage"));
    if (self.danmakus.count && self.lineHeight && self.duration && self.maxShowLineCount) {
        return YES;
    }
    return NO;
}

- (void)start {
    if (_isPauseing) [self resume];

    if ([self isPrepared]) {
        if (!_timer) {
            _timer = [NSTimer timerWithTimeInterval:timeMargin target:self selector:@selector(getCurrentTime) userInfo:nil repeats:YES];
            [[NSRunLoop currentRunLoop] addTimer:_timer forMode:NSRunLoopCommonModes];
            [_timer fire];
        }
    }
}
- (void)pause {
    if (!_timer || !_timer.isValid) return;

    _isPauseing = YES;
    _isPlaying  = NO;

    [_timer invalidate];
    _timer = nil;

    for (UILabel* label in self.subviews) {
        CALayer* layer = label.layer;
        CGRect   rect  = label.frame;
        if (layer.presentationLayer) {
            rect = ((CALayer*)layer.presentationLayer).frame;
        }
        label.frame = rect;
        [label.layer removeAllAnimations];
    }
}
- (void)resume {
    if (![self isPrepared] || _isPlaying || !_isPauseing) return;
    for (CFDanmakuInfo* info in self.subDanmakuInfos) {
        if (info.danmaku.position == CFDanmakuPositionNone) {
            [self performAnimationWithDuration:info.leftTime danmakuInfo:info];
        } else {
            _isPauseing = NO;
            [self performCenterAnimationWithDuration:info.leftTime danmakuInfo:info];
        }
    }

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(timeMargin * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self start];
    });
}
- (void)stop {
    _isPauseing = NO;
    _isPlaying  = NO;

    [_timer invalidate];
    _timer = nil;
    [self.danmakus removeAllObjects];
    self.linesDict = nil;
}

- (void)clear {
    [_timer invalidate];
    _timer         = nil;
    self.linesDict = nil;
    _isPauseing    = YES;
    _isPlaying     = NO;
    [self.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
}

- (void)sendDanmakuSource:(CFDanmaku*)danmaku {
    // 关闭弹幕，不允许添加弹幕
    if (!self.danmakuVisible) {
            NSLog(@"❌ 弹幕不可见，不添加: %@", danmaku.contentStr.string);
            return;
    }
    [self playDanmaku:danmaku];
}

- (void)showDanmaku {
    if (self.danmakuVisible) return;
    NSLog(@"打开弹幕 log");

    self.danmakuVisible = YES;
    [self start]; // 重新启动弹幕
}

- (void)hideDanmaku {
    if (!self.danmakuVisible) return;
    NSLog(@"关闭弹幕 log");
    
    self.danmakuVisible = NO;
    
    // 1. 立即清除屏幕上所有的弹幕 label
    for (UIView *subview in self.subviews) {
        if ([subview isKindOfClass:[UILabel class]]) {
            [subview.layer removeAllAnimations];
            [subview removeFromSuperview];
        }
    }

    // 2. 清除当前正在播放的弹幕信息
    for (NSMutableArray *lineInfos in self.linesDict.allValues) {
        [lineInfos removeAllObjects];
    }
    
    // 3. 清除等待播放的弹幕队列（防止后续还继续播放）
    [self.waitingQueue clear];
    
}

- (void)setDensity:(CFDanmakuDensity)density inFrame:(CGRect)frame {
    CGFloat totalHeight = CGRectGetHeight(frame);
    CGFloat usedHeight = totalHeight;

    switch (density) {
        case CFDanmakuDensityMedium:
            usedHeight = totalHeight * 0.5;
            break;
        case CFDanmakuDensityHigh:
            usedHeight = totalHeight * 1.0;
            break;
    }

    NSInteger maxLines = (NSInteger)(usedHeight / (self.lineHeight + self.lineMargin));
    self.maxShowLineCount = MAX(1, maxLines);

    NSLog(@"弹幕轨道动态调整：density=%ld，高度=%.2f，轨道数=%ld", (long)density, usedHeight, (long)self.maxShowLineCount);
}

@end
