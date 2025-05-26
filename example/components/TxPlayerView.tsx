import React, { useMemo, useRef, useEffect } from "react";
import { StyleSheet, StyleProp, ViewStyle } from "react-native";
import ExpoTxPlayer, { ExpoTxPlayerView } from "expo-tx-player";
import { Player, destroyPlayer } from "./Player";

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
  useEffect(() => {
    return () => {
      destroyPlayer();
    };
  }, []);
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
