#import "CFDanmakuQueue.h"
#import "CFDanmakuInfo.h" // 确保你引入这个类

@interface CFDanmakuQueue ()
@property(nonatomic, strong) NSMutableArray<CFDanmakuInfo *> *queue;
@end

@implementation CFDanmakuQueue

- (instancetype)init {
    if (self = [super init]) {
        _queue = [NSMutableArray array];
    }
    return self;
}

- (void)enqueue:(CFDanmakuInfo *)info {
    [self.queue addObject:info];
}

- (CFDanmakuInfo *)dequeue {
    if (self.queue.count == 0) return nil;
    CFDanmakuInfo *first = self.queue.firstObject;
    [self.queue removeObjectAtIndex:0];
    return first;
}

- (CFDanmakuInfo *)peek {
    if (self.queue.count == 0) return nil;
    return self.queue.firstObject;
}

- (BOOL)isEmpty {
    return self.queue.count == 0;
}

- (NSInteger)count {
    return self.queue.count;
}

- (void)clear {
    [self.queue removeAllObjects];
}

@end
