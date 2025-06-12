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
import { useEffect, useState } from "react";
import { useTxPlayer } from "./components/useTxPlayer";
import { PlayerView } from "./components";

const hls =
  "https://tpull-uat.uipqub.com/live/test.m3u8?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971";

const rtc =
  "webrtc://tpull-uat.uipqub.com/live/test?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971";

let intervalId: NodeJS.Timeout | null = null;
let count = 0;

const colors = [
  "red",
  "blue",
  "green",
  "yellow",
  "white",
  "black",
  "gray",
  "orange",
  "purple",
];

export default function App() {
  const [danmu, setDanmu] = useState("这是一条JS端发送的弹幕");
  const startFiring = () => {
    if (intervalId) return;

    count = 0; // 重置计数
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
      const color = colors[count % colors.length]; // 每条弹幕换颜色

      ExpoTxPlayer.sendDanmaku(`${emoji} 弹幕 ${count++} ${suf}`, color);
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
  const [inputUrl, setInputUrl] = useState("");

  const player = useTxPlayer(rtc, (player) => {
    player.play();
  });

  player.muted = true;

  const isPlaying = useEvent(player, "playingChange", player.playing);
  // console.log(isPlaying, "playingChange");
  const error = useEvent(player, "error", null);
  // console.log(error, "error");

  const status = useEvent(player, "statusChange", player.status);

  // console.log(status, "statusChange");

  return (
    <SafeAreaView style={{ paddingTop: 48 }}>
      <PlayerView
        player={player}
        style={{ width: "100%", height: 800 }}
        contentFit="contain"
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

      <Button title="开始模拟高密度弹幕" onPress={startFiring} />

      {/* <Button title="停止弹幕非关闭弹幕" onPress={stopFiring} /> */}

      <TextInput
        value={danmu}
        onChangeText={setDanmu}
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
      <View style={{ padding: 16 }}>
        <Text style={{ color: "black", marginBottom: 8 }}>
          当前播放源: {player.url}
        </Text>
        <TextInput
          placeholder="输入播放源 URL"
          placeholderTextColor="#999"
          value={inputUrl}
          onChangeText={setInputUrl}
          style={{
            height: 40,
            borderColor: "#ccc",
            borderWidth: 1,
            paddingHorizontal: 10,
            color: "black",
            marginBottom: 8,
          }}
        />
        <Button
          title="切换播放源"
          onPress={() => {
            player.switchSource(inputUrl);
            // setCurrentUrl(inputUrl);
          }}
        />
      </View>
    </SafeAreaView>
  );
}
