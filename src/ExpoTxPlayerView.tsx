import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoTxPlayerViewProps } from './ExpoTxPlayer.types';

const NativeView: React.ComponentType<ExpoTxPlayerViewProps> =
  requireNativeView('ExpoTxPlayer');

export default function ExpoTxPlayerView(props: ExpoTxPlayerViewProps) {
  return <NativeView {...props} />;
}
