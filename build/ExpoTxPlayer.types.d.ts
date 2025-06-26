import type { StyleProp, ViewStyle } from "react-native";
export type OnLoadEventPayload = {
    url: string;
};
export type ExpoTxPlayerModuleEvents = {};
export type ChangeEventPayload = {};
export type ExpoTxPlayerViewProps = {
    url: string;
    style?: StyleProp<ViewStyle>;
    contentFit?: "contain" | "cover" | "fill";
    allowsPictureInPicture?: boolean;
    allowsFullscreen?: boolean;
    onCastButtonPressed?: () => void;
    onFullscreenEnter?: () => void;
    onFullscreenEnd?: () => void;
    onPIPStart?: () => void;
    onPIPStop?: () => void;
    onBack?: () => void;
    onError?: (e: {
        nativeEvent: {
            message: string;
        };
    }) => void;
    onStatusChange?: (e: {
        nativeEvent: {
            status: string;
        };
    }) => void;
    onBufferedChange?: (e: {
        nativeEvent: {
            value: number;
        };
    }) => void;
    onPlayingChange?: (e: {
        nativeEvent: {
            value: boolean;
        };
    }) => void;
};
//# sourceMappingURL=ExpoTxPlayer.types.d.ts.map