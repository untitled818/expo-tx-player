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

ExpoTxPlayer.setLicense({
  url: "https://license.vod2.myqcloud.com/license/v2/1308280968_1/v_cube.license",
  key: "b371c6eb2f0a78f3a61b840db671f058",
  appId: 1308280968,
});
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
let intervalId: NodeJS.Timeout | null = null;
let count = 0;

export default function App() {
  const startFiring = () => {
    if (intervalId) return;

    count = 0; // 重置计数
    // 规整弹幕
    // intervalId = setInterval(() => {
    //   if (count >= 50) {
    //     stopFiring();
    //     return;
    //   }
    //   ExpoTxPlayer.sendDanmaku(`🔥 弹幕 ${count++}`);
    // }, 200); // 每 200 毫秒发一条（5 条/秒）

    // 不规整弹幕
    intervalId = setInterval(() => {
      // if (count >= 50) {
      //   stopFiring();
      //   return;
      // }

      const emojis = ["🔥", "💥", "⚡️", "🎯", "🚀"];
      const suffix = [
        "来了乐乐乐乐乐乐乐了了",
        "冲了",
        "TV streaming device by Google",
        "再来一发火火火火火火火火火",
        "爆炸啦",
      ];
      const emoji = emojis[Math.floor(Math.random() * emojis.length)];
      const suf = suffix[Math.floor(Math.random() * suffix.length)];

      ExpoTxPlayer.sendDanmaku(`${emoji} 弹幕 ${count++} ${suf}`, "green");
    }, 200);
  };

  const stopFiring = () => {
    if (intervalId) {
      clearInterval(intervalId);
      intervalId = null;
    }
  };

  useEffect(() => {
    return () => {
      stopFiring(); // 组件卸载时清除定时器
    };
  }, []);
  const player = useTxPlayer(rtc, (player) => {
    player.play();
  });

  // const player = useTxPlayer(rtc, (player) => {
  //   player.play();
  // });

  // const isPlaying = useEvent(player, "playingChange", player.playing);
  // console.log(isPlaying, "playingChange");
  // const error = useEvent(player, "error", null);
  // console.log(error, "error");

  // const status = useEvent(player, "statusChange", player.status);

  const [danmu, setDanmu] = useState("这是一条JS端发送的弹幕");

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ExpoTxPlayerView url={hls} style={{ width: "100%", flex: 1 }} />

      {/* 打开和关闭弹幕测试 */}

      <Button title="打开弹幕" onPress={() => ExpoTxPlayer.showDanmaku()} />

      <Button title="关闭弹幕" onPress={() => ExpoTxPlayer.hideDanmaku()} />

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

      <TextInput
        value={danmu}
        onChangeText={setDanmu} // ✅ 这是 React Native 的写法
        placeholder="请输入弹幕"
        style={{ borderWidth: 1, padding: 8 }}
      />

      <Button
        title="发送弹幕"
        onPress={() => {
          ExpoTxPlayer.sendDanmaku(danmu, "blue", true);
          setDanmu("");
        }}
      />

      <Button title="开始模拟高密度弹幕" onPress={startFiring} />

      <Button title="停止弹幕" onPress={stopFiring} />

      <Button title="暂停弹幕" onPress={() => ExpoTxPlayer.pauseDanmaku()} />

      <ScrollView>
        <Text>Module API Example</Text>
      </ScrollView>
    </SafeAreaView>
  );
}
