// Reexport the native module. On web, it will be resolved to ExpoTxPlayerModule.web.ts
// and on native platforms to ExpoTxPlayerModule.ts
export { default } from "./ExpoTxPlayerModule";
export { default as ExpoTxPlayerView } from "./ExpoTxPlayerView";
export * from "./ExpoTxPlayer.types";
