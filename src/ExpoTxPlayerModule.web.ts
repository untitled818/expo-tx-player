import { registerWebModule, NativeModule } from 'expo';

import { ExpoTxPlayerModuleEvents } from './ExpoTxPlayer.types';

class ExpoTxPlayerModule extends NativeModule<ExpoTxPlayerModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoTxPlayerModule, 'ExpoTxPlayerModule');
