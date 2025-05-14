import * as React from 'react';

import { ExpoTxPlayerViewProps } from './ExpoTxPlayer.types';

export default function ExpoTxPlayerView(props: ExpoTxPlayerViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
