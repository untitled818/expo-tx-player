//
//  PaddingLabel.m
//  Pods
//
//  Created by fin on 7.6.2025.
//

#import "PaddingLabel.h"

@implementation PaddingLabel

- (instancetype)init {
    if (self = [super init]) {
        self.textInsets = UIEdgeInsetsMake(4, 8, 4, 8);
    }
    return self;
}

- (void)drawTextInRect:(CGRect)rect {
    [super drawTextInRect:UIEdgeInsetsInsetRect(rect, self.textInsets)];
}

- (CGSize)intrinsicContentSize {
    CGSize size = [super intrinsicContentSize];
    size.width += self.textInsets.left + self.textInsets.right;
    size.height += self.textInsets.top + self.textInsets.bottom;
    return size;
}

@end
