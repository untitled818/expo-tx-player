import { NativeModule, requireNativeModule } from 'expo';

import { ExpoTxPlayerModuleEvents } from './ExpoTxPlayer.types';

declare class ExpoTxPlayerModule extends NativeModule<ExpoTxPlayerModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoTxPlayerModule>('ExpoTxPlayer');
