import { useEvent } from "expo";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import { Button, SafeAreaView, ScrollView, Text, View } from "react-native";

ExpoTxPlayer.setLicense({
  url: "https://license.vod2.myqcloud.com/license/v2/1258384072_1/v_cube.license",
  key: "4c71bd88da95af202a8f3b2743c7e4e4",
});

export default function App() {
  // const onChangePayload = useEvent(ExpoTxPlayer, 'onChange');

  return (
    <SafeAreaView>
      <ExpoTxPlayerView
        url="webrtc://tpull-uat.uipqub.com/live/test?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971"
        style={{ width: "100%", height: 200 }}
      />
      <ScrollView>
        <Text>Module API Example</Text>
      </ScrollView>
    </SafeAreaView>
  );
}
