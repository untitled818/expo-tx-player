import { useEvent } from "expo";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import { Button, SafeAreaView, ScrollView, Text, View } from "react-native";

ExpoTxPlayer.setLicense({
  url: "https://license.vod2.myqcloud.com/license/v2/1258384072_1/v_cube.license",
  key: "4c71bd88da95af202a8f3b2743c7e4e4",
});

const hls =
  "https://tpull-uat.uipqub.com/live/test.m3u8?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971";

const rtc =
  "webrtc://tpull-uat.uipqub.com/live/test?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971";

export default function App() {
  // const onChangePayload = useEvent(ExpoTxPlayer, 'onChange');

  return (
    <SafeAreaView>
      <ExpoTxPlayerView
        url={hls}
        style={{ width: "100%", height: 200 }}
        onFullscreenEnter={() => {
          console.log("onFullscreen");
        }}
        onFullscreenEnd={() => {
          console.log("onFullscreenEnd");
        }}
        onPIPStart={() => {
          console.log("pip Start");
        }}
        onPIPStop={() => {
          console.log("pip Stop");
        }}
        onCastButtonPressed={() => {
          console.log("cast Button Pressed");
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

      <ScrollView>
        <Text>Module API Example</Text>
      </ScrollView>
    </SafeAreaView>
  );
}
