import type { StyleProp, ViewStyle } from "react-native";

export type OnLoadEventPayload = {
  url: string;
};

export type ExpoTxPlayerModuleEvents = {};

export type ChangeEventPayload = {};

export type ExpoTxPlayerViewProps = {
  url: string;
  style?: StyleProp<ViewStyle>;
};
