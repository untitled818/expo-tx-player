#import "txplayer.h"
#import <SuperPlayer/SuperPlayer.h>

@interface TXPlayerWrapper ()

@property (nonatomic, strong) TXLivePlayer *player;

@end

@implementation TXPlayerWrapper

+ (instancetype)shared {
  static TXPlayerWrapper *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [[self alloc] init];
  });
  return sharedInstance;
}

- (instancetype)init {
  if (self = [super init]) {
    _player = [[TXLivePlayer alloc] init];
  }
  return self;
}

- (void)play:(NSString *)url {
  [_player startPlay:url type:PLAY_TYPE_VOD_MP4];
}

- (void)pause {
  [_player pause];
}

- (void)resume {
  [_player resume];
}

- (void)stop {
  [_player stopPlay];
}

@end
