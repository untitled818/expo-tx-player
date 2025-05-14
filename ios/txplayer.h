#import <Foundation/Foundation.h>

@interface TXPlayerWrapper : NSObject

+ (instancetype)shared;

- (void)play:(NSString *)url;
- (void)pause;
- (void)resume;
- (void)stop;

@end
