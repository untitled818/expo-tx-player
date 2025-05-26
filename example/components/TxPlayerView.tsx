import React, { useMemo, useRef } from "react";
import { StyleSheet, StyleProp, ViewStyle } from "react-native";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import { Player } from "./Player";

// ExpoTxPlayer.setLicense({
//   url: "https://license.vod-control.com/license/v2/1315081628_1/v_cube.license",
//   key: "589c3bc57bfdf9a4ecd75687b163a054",
//   appId: "f0039500001",
// });

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
