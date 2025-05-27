import { useEvent } from "expo";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import {
  Button,
  SafeAreaView,
  ScrollView,
  Text,
  TextInput,
  View,
} from "react-native";
import { useTxPlayer } from "./components/useTxPlayer";
import { PlayerView } from "./components";
import { useEffect, useState } from "react";

// ExpoTxPlayer.setLicense({
//   url: "https://license.vod2.myqcloud.com/license/v2/1258384072_1/v_cube.license",
//   key: "4c71bd88da95af202a8f3b2743c7e4e4",
// });

const hls =
  "https://tpull-uat.uipqub.com/live/test.m3u8?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971";

const rtc =
  "webrtc://tpull-uat.uipqub.com/live/test?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971";

const videoObj = {
  source: hls,
  url: "https://license.vod-control.com/license/v2/1315081628_1/v_cube.license",
  key: "589c3bc57bfdf9a4ecd75687b163a054",
  appId: "f0039500001",
};

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

  const [danmu, setDanmu] = useState("这是一条JS端发送的弹幕");

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

      {/* <TextInput value={danmu} onChange={(e) =>} /> */}

      <Button title="发送弹幕" onPress={ExpoTxPlayer.sendDanmaku(danmu)} />

      <ScrollView>
        <Text>Module API Example</Text>
      </ScrollView>
    </SafeAreaView>
  );
}
