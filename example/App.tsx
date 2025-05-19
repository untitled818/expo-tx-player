import { useEvent } from "expo";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import { Button, SafeAreaView, ScrollView, Text, View } from "react-native";
import { useState, useEffect, useRef } from "react";

export default function App() {
  const onChangePayload = useEvent(ExpoTxPlayer, "onChange");

  const [url, setUrl] = useState<string | null>(null);

  useEffect(() => {
    // 延迟到下一帧或视图 layout 后再设置 url
    const timeout = setTimeout(() => {
      setUrl(
        "webrtc://tpull-uat.uipqub.com/live/test?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971"
      );
    }, 100); // 延迟 100ms，确保 native view attach

    return () => clearTimeout(timeout);
  }, []);
  return (
    <SafeAreaView style={styles.container}>
      {/* {url && (
        <ExpoTxPlayerView
          url={url}
          style={{ flex: 1 }}
          onLoad={({ nativeEvent }) =>
            console.log(`Loaded: ${nativeEvent.url}`)
          }
        />
      )} */}
      <ExpoTxPlayerView
        url="webrtc://tpull-uat.uipqub.com/live/test?txSecret=84fa018ec80b3fe2195036ca94e8d6d7&txTime=69E98971"
        onLoad={({ nativeEvent: { url } }) => console.log(`Loaded: ${url}`)}
        style={styles.view}
      />
      <ScrollView style={styles.container}>
        <Text style={styles.header}>Module API Example</Text>
        <Group name="Constants">
          <Text>{ExpoTxPlayer.PI}</Text>
        </Group>
        {/* <Group name="Functions"> */}
        {/* <Text>{ExpoTxPlayer.hello()}</Text> */}
        {/* </Group> */}
        <Group name="Async functions">
          <Button
            title="Set value"
            onPress={async () => {
              // await ExpoTxPlayer.setValueAsync('Hello from JS!');
            }}
          />
        </Group>
        <Group name="Events">
          <Text>{onChangePayload?.value}</Text>
        </Group>
        <Group name="Views"></Group>
      </ScrollView>
    </SafeAreaView>
  );
}

function Group(props: { name: string; children: React.ReactNode }) {
  return (
    <View style={styles.group}>
      <Text style={styles.groupHeader}>{props.name}</Text>
      {props.children}
    </View>
  );
}

const styles = {
  header: {
    fontSize: 30,
    margin: 20,
  },
  groupHeader: {
    fontSize: 20,
    marginBottom: 20,
  },
  group: {
    margin: 20,
    backgroundColor: "#fff",
    borderRadius: 10,
    padding: 20,
  },
  container: {
    flex: 1,
    backgroundColor: "#eee",
  },
  view: {
    flex: 1,
    // height: 200,
  },
};
