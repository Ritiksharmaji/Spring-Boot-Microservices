import {NativeModules, Platform} from 'react-native';

/**
 * Runtime configuration. A mobile app ships no secrets, so plain constants selected by build type
 * (__DEV__) are enough: debug builds talk to the backend on your computer, release builds (APK/AAB)
 * to the deployed API on AWS.
 */

/** Deployed Spring Boot API (backend/monolith on the Lightsail apps-server). */
export const PRODUCTION_API_URL = 'https://shop-api.onekampus.com';

/** Port of the Spring Boot API when it runs locally (backend/monolith, `server.port`). */
const DEV_BACKEND_PORT = 8086;

/**
 * Force one backend in every build, e.g. PRODUCTION_API_URL to test the deployed API from a debug
 * build, or 'http://192.168.1.10:8086'. null = automatic.
 */
const API_URL_OVERRIDE: string | null = null;

/** Host of the Metro bundler the debug JS bundle was loaded from (your computer). */
const getMetroHost = (): string | undefined => {
  const sourceCode = NativeModules.SourceCode;
  const scriptURL: string | undefined =
    sourceCode?.getConstants?.().scriptURL ?? sourceCode?.scriptURL;
  return scriptURL?.match(/^https?:\/\/([^:/]+)/)?.[1];
};

const getDevApiUrl = (): string => {
  const host = getMetroHost();
  if (host) {
    // Same machine as Metro. For "localhost" (USB / emulator) run `npm run android:reverse`.
    return `http://${host}:${DEV_BACKEND_PORT}`;
  }
  return Platform.select({
    android: `http://10.0.2.2:${DEV_BACKEND_PORT}`, // Android emulator -> computer's localhost
    default: `http://localhost:${DEV_BACKEND_PORT}`,
  });
};

/** Web shop (frontend/web). Also serves the demo product images ("/products/..."). */
const WEBSITE_URL = 'https://shop.onekampus.com';

export const env = {
  apiUrl: API_URL_OVERRIDE ?? (__DEV__ ? getDevApiUrl() : PRODUCTION_API_URL),
  isProduction: !__DEV__,
  websiteUrl: WEBSITE_URL,
  privacyPolicyUrl: `${WEBSITE_URL}/privacy`,
} as const;
