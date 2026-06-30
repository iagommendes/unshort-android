import { registerWebModule, NativeModule } from 'expo';

class UnshortCoreModule extends NativeModule<{}> {}

export default registerWebModule(UnshortCoreModule, 'UnshortCoreModule');
