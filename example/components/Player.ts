// Player.ts
import ExpoTxPlayer from "expo-tx-player";
import type { EventEmitter } from "events";

export type PlayerEventMap = {
  playingChange: boolean;
  error: { message: string };
  statusChange: string;
  bufferedChange: number;
};

type PlayerListeners = {
  [K in keyof PlayerEventMap]?: Listener<PlayerEventMap[K]>[];
};

type Listener<T> = (value: T) => void;

export interface TypedEventEmitter<T> {
  addListener<K extends keyof T>(event: K, cb: (value: T[K]) => void): void;
  removeListener<K extends keyof T>(event: K, cb: (value: T[K]) => void): void;
  removeAllListeners<K extends keyof T>(event?: K): void;
  listenerCount<K extends keyof T>(event: K): number;
}

// ExpoTxPlayer.setLicense({
//   url: "https://license.vod-control.com/license/v2/1315081628_1/v_cube.license",
//   key: "589c3bc57bfdf9a4ecd75687b163a054",
//   appId: "f0039500001",
// });

let currentPlayer: Player | null = null;

type videoType = {
  source: string;
  url: string;
  key: string;
  appId: string;
};

export class Player implements TypedEventEmitter<PlayerEventMap> {
  public url: string;
  public loop = false;
  public playing = false;
  public _status = "unknown";
  public _buffered = 0;
  private _muted = false;
  private _volume = 50; // 1 - 100

  private listeners: PlayerListeners = {};

  // ExpoTxPlayer.setLicense({
  //   url: video.url,
  //   key: video.key,
  // });

  constructor(url: string) {
    this.url = url;
    this._status = ExpoTxPlayer.getStatus();

    ExpoTxPlayer.setLicense({
      url: "https://license.vod2.myqcloud.com/license/v2/1258384072_1/v_cube.license",
      key: "4c71bd88da95af202a8f3b2743c7e4e4",
    });
  }

  // static source(url: string) {
  //   return new Player(url);
  // }

  static source(url: string): Player {
    if (currentPlayer) {
      currentPlayer.switchSource(url); // ✅ 更新 URL 而不是 new
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
    // TODO: call native switchSource
    ExpoTxPlayer.setVideoURL(url);
  }

  addListener<K extends keyof PlayerEventMap>(
    event: K,
    cb: Listener<PlayerEventMap[K]>
  ) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event]!.push(cb);
    return this;
  }

  removeListener<K extends keyof PlayerEventMap>(
    event: K,
    cb: Listener<PlayerEventMap[K]>
  ) {
    const arr = this.listeners[event];
    if (!arr) return this;
    (this.listeners as any)[event] = arr.filter(
      (listener) => listener !== cb
    ) as Listener<PlayerEventMap[K]>[];
    return this;
  }

  removeAllListeners<K extends keyof PlayerEventMap>(event?: K) {
    if (event) {
      delete this.listeners[event];
    } else {
      this.listeners = {};
    }
    return this;
  }

  listenerCount<K extends keyof PlayerEventMap>(event: K): number {
    return this.listeners[event]?.length ?? 0;
  }

  emit<K extends keyof PlayerEventMap>(event: K, value: PlayerEventMap[K]) {
    const arr = this.listeners[event];
    arr?.forEach((cb) => cb(value));
    return true;
  }

  // Compatibility aliases
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
