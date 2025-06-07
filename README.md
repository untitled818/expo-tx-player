# expo-tx-player

## 使用方式

建议创建 `Player.ts`  状态转发, `TxPlayerView.tsx` 视图组件, `useTxPlayer.ts` 创建实例

Player.ts
```ts
// Player.ts
import ExpoTxPlayer from "expo-tx-player";

export type PlayerEventMap = {
  playingChange: boolean;
  error: { message: string };
  statusChange: string;
  bufferedChange: number;
};

type Listener<T> = (value: T) => void;

export interface EventSubscription {
  remove(): void;
}

export interface EventEmitterCompatible {
  addListener(event: string, cb: (value: any) => void): EventSubscription;
  removeListener(event: string, cb: (value: any) => void): void;
}

let currentPlayer: Player | null = null;

export function destroyPlayer() {
  if (currentPlayer) {
    console.log("[Player] 🧹 清理播放器实例");
    currentPlayer.pause();
    currentPlayer.removeAllListeners();
    currentPlayer = null;
    ExpoTxPlayer.resetPlayer();
  }
}

type PlayerListeners = {
  [event: string]: Listener<any>[];
};

type videoType = {
  source: string;
  url: string;
  key: string;
  appId: string;
};

export class Player implements EventEmitterCompatible {
  public url: string;
  public loop = false;
  public playing = false;
  public _status = "unknown";
  public _buffered = 0;
  private _muted = false;
  private _volume = 50; // 1 - 100

  private listeners: PlayerListeners = {};

  constructor(url: string) {
    this.url = url;
    this._status = ExpoTxPlayer.getStatus();

    ExpoTxPlayer.setLicense({
      url: "",
      key: "",
    });
  }

  static source(url: string): Player {
    if (currentPlayer) {
      currentPlayer.switchSource(url);
      return currentPlayer;
    }

    const p = new Player(url);
    currentPlayer = p;
    return p;
  }

  get muted() {
    return this._muted;
  }

  get bufferedPosition() {
    this._buffered = ExpoTxPlayer.bufferedPosition();
    return this._buffered;
  }

  set muted(value: boolean) {
    this._muted = value;
    console.log("RN muted 设置为", value);
    ExpoTxPlayer.setMute(value);
  }

  get volume() {
    return this._volume;
  }

  set volume(value: number) {
    this._volume = value;
    console.log("RN 音量 设置为", value);
    ExpoTxPlayer.setVolume(value);
  }

  get status() {
    return ExpoTxPlayer.getStatus();
  }

  play() {
    this.playing = true;
    this.emit("playingChange", true);
    ExpoTxPlayer.play();
  }

  pause() {
    this.playing = false;
    this.emit("playingChange", false);
    ExpoTxPlayer.pause();
  }

  switchSource(url: string) {
    this.url = url;
    ExpoTxPlayer.setVideoURL(url);
  }

  addListener(event: string, cb: (value: any) => void): EventSubscription {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(cb);

    return {
      remove: () => {
        this.removeListener(event, cb);
      },
    };
  }

  removeListener(event: string, cb: (value: any) => void): void {
    const arr = this.listeners[event];
    if (!arr) return;
    this.listeners[event] = arr.filter((listener) => listener !== cb);
  }

  removeAllListeners(event?: string): void {
    if (event) {
      delete this.listeners[event];
    } else {
      this.listeners = {};
    }
  }

  listenerCount(event: string): number {
    return this.listeners[event]?.length ?? 0;
  }

  emit(event: string, value: any): boolean {
    const arr = this.listeners[event];
    arr?.forEach((cb) => cb(value));
    return true;
  }

  on = this.addListener;
  off = this.removeListener;

  _onNativeError(message: string) {
    this.emit("error", { message });
  }

  _onNativeStatus(status: string) {
    this._status = status;
    this.emit("statusChange", status);
  }

  _onNativeBuffered(buffered: number) {
    this._buffered = buffered;
    this.emit("bufferedChange", buffered);
  }

  _onNativePlayingChange(value: boolean) {
    this.playing = value;
    this.emit("playingChange", value);
  }
}

```

TxPlayerView.tsx

```tsx
import React, { useMemo, useRef, useEffect } from "react";
import { StyleSheet, StyleProp, ViewStyle } from "react-native";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import { Player, destroyPlayer } from "./Player";

type Props = {
  style?: StyleProp<ViewStyle>;
  player: Player;
  contentFit?: "contain" | "cover" | "fill";
  allowsPictureInPicture?: boolean;
  allowsFullscreen?: boolean;
  onFullscreenEnter?: () => void;
  onFullscreenEnd?: () => void;
  onPIPStart?: () => void;
  onPIPStop?: () => void;
  onScreenCastStart?: () => void;
  onScreenCastStop?: () => void;
};

export const PlayerView: React.FC<Props> = ({
  player,
  style,
  contentFit = "contain",
  allowsPictureInPicture = true,
  allowsFullscreen = true,
  onFullscreenEnter,
  onFullscreenEnd,
  onPIPStart,
  onPIPStop,
  onScreenCastStart,
  onScreenCastStop,
}) => {
  const castModalRef = useRef<any>(null);
  useEffect(() => {
    return () => {
      destroyPlayer();
    };
  }, []);
  return (
    <>
      <ExpoTxPlayerView
        onCastButtonPressed={() => {
          castModalRef.current?.show();
          castModalRef.current?.startSearch();
        }}
        onFullscreenEnter={onFullscreenEnter}
        onFullscreenEnd={onFullscreenEnd}
        onPIPStart={onPIPStart}
        onPIPStop={onPIPStop}
        style={[style]}
        url={player.url}
        contentFit={contentFit}
        allowsPictureInPicture={allowsPictureInPicture}
        allowsFullscreen={allowsFullscreen}
        onError={(e) => player._onNativeError(e.nativeEvent.message)}
        onStatusChange={(e) => player._onNativeStatus(e.nativeEvent.status)}
        onBufferedChange={(e) => player._onNativeBuffered(e.nativeEvent.value)}
        onPlayingChange={(e) =>
          player._onNativePlayingChange(e.nativeEvent.value)
        }
      />
    </>
  );
};

const styles = StyleSheet.create({});

```

useTxPlayer.ts
```ts
import { useMemo } from "react";
import { Player } from "./Player";

export function useTxPlayer(
  source: string,
  setup?: (player: Player) => void
): Player {
  const player = useMemo(() => {
    const p = Player.source(source);
    setup?.(p);
    return p;
  }, [source]);

  return player;
}
```

- 使用该组件

```tsx
export default function App() {
  const player = useTxPlayer(rtc, (player) => {
    player.play();
  });

  const isPlaying = useEvent(player, "playingChange", player.playing);
  console.log(isPlaying, "playingChange");
  const error = useEvent(player, "error", null);
  console.log(error, "error");

  const status = useEvent(player, "statusChange", player.status);

  console.log(status, "statusChange");

  return (
    <SafeAreaView>
      <PlayerView
        player={player}
        style={{ width: "100%", height: 200 }}
        contentFit="cover"
        onFullscreenEnter={() => {
          console.log("fullscreen start");
        }}
        onFullscreenEnd={() => {
          console.log("fullscreen stop");
        }}
        onPIPStart={() => {
          console.log("pip start");
        }}
        onPIPStop={() => {
          console.log("pip end");
        }}
        onScreenCastStart={() => {
          console.log("cast start");
        }}
        onScreenCastStop={() => {
          console.log("cast stop");
        }}
      />

      <Button
        title={isPlaying ? "Pause" : "Play"}
        onPress={() => {
          if (isPlaying) {
            player.pause();
          } else {
            player.play();
          }
        }}
      />

      <Button
        title="音量"
        onPress={() => {
          ExpoTxPlayer.setMute(true);
        }}
      />
      <Button
        title="播放"
        onPress={() => {
          ExpoTxPlayer.bufferedPosition();
        }}
      />

      <Button
        title="获取到当前状态"
        onPress={() => {
          console.log(ExpoTxPlayer.getStatus());
        }}
      />

      <Button
        title="切换视频源hls"
        onPress={() => {
          player.switchSource(hls);
        }}
      />
      <Button
        title="切换视频源rtc"
        onPress={() => {
          player.switchSource(rtc);
        }}
      />
    </SafeAreaView>
  );
}
```