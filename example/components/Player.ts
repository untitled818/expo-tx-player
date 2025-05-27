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

export class Player implements EventEmitterCompatible {
  private _url: string;
  public _playing = false;
  private _status = "unknown";
  private _buffered = 0;
  private _muted = false;
  private _volume = 50; // 1 - 100

  private listeners: PlayerListeners = {};

  constructor(url: string) {
    this._url = url;
    this._status = ExpoTxPlayer.getStatus();

    ExpoTxPlayer.setLicense({
      url: "https://license.vod-control.com/license/v2/1315081628_1/v_cube.license",
      key: "589c3bc57bfdf9a4ecd75687b163a054",
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

  get url() {
    return this._url;
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

  get playing() {
    return this._playing;
  }

  play() {
    this._playing = true;
    this.emit("playingChange", true);
    ExpoTxPlayer.play();
  }

  pause() {
    this._playing = false;
    this.emit("playingChange", false);
    ExpoTxPlayer.pause();
  }

  switchSource(url: string) {
    this._url = url;
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
    this._playing = value;
    this.emit("playingChange", value);
  }
}
