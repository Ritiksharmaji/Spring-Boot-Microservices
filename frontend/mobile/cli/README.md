# E-commerce Mobile App - React Native CLI

The React Native CLI version of the e-commerce app (same screens and features as `../expo`), built
with Gradle / Xcode instead of Expo. React Native 0.81.5, React 19.1, New Architecture, Hermes.

| | |
|---|---|
| Android package / iOS bundle id | `com.ritikji.clientEcommerce` |
| App name on the phone | `oneKampus Shop` (`android/app/src/main/res/values/strings.xml`, iOS `Info.plist`) |
| Backend (release builds) | `https://shop-api.onekampus.com` — Spring Boot, `backend/monolith` (`src/config/env.ts`) |
| Backend (debug builds) | port 8086 on the computer running Metro |
| Images | demo catalogue images (`/products/...`) load from `https://shop.onekampus.com`; admin uploads from the API |

## How it talks to the Spring Boot API

`src/services/api/*` call the endpoints in [`backend/monolith/docs/API.md`](../../../backend/monolith/docs/API.md);
`src/utils/normalize.ts` maps the API's JSON to the app's models (same idea as `frontend/web/src/api/mappers.js`).

| App feature | API |
|---|---|
| Sign in / sign up / profile | `POST /api/auth/login`, `POST /api/auth/register` (name split into first/last), `GET /api/users/me` |
| Delete account (Profile) | `DELETE /api/users/me {password}` — customers only |
| Shop, product, search | `GET /api/products?size=500` (filtered and paged in the app) |
| Cart (per size) | `/api/cart`, `/api/cart/items/{productId}?size=` |
| Wishlist | `/api/wishlist` (`PUT` / `DELETE /api/wishlist/{productId}`) |
| Checkout | `POST /api/orders` with recipient + address; **Cash on delivery** or **Card** (simulated, approved up to ₹50,000) |
| Orders | `GET /api/orders`, `GET /api/orders/{id}`, `POST /api/orders/{id}/cancel` (pending / confirmed) |
| Admin dashboard | `GET /api/users`, products and `GET /api/orders/admin` (numbers computed in the app) |
| Admin orders | `PATCH /api/orders/{id}/status` — only the next steps are offered: pending → confirmed → shipped → delivered, or cancelled |
| Admin products | gallery photos → `POST /api/products/images` (multipart), then `POST` / `PUT /api/products` with the URLs; delete = hide |

Prices are in ₹ and delivery is free (the API's order total is the sum of the items).

## Project structure

Feature-based layout with a typed navigation tree, a service layer for every API call and a
single theme shared by NativeWind and code.

```
cli/
├── index.js                       registers src/app/App
├── src/
│   ├── app/                       App.tsx (root), AppProviders.tsx (auth → cart → wishlist)
│   ├── config/env.ts              backend URL per build type (debug = your computer, release = AWS)
│   ├── theme/                     colors.json (also feeds tailwind.config.js) + typed `colors`
│   ├── constants/                 app values, categories, order-status styles, storage keys
│   ├── types/                     domain models (Product, Order, ...) and API envelopes
│   ├── services/
│   │   ├── api/                   axios client + authApi, productApi, cartApi, wishlistApi,
│   │   │                          orderApi, addressApi, adminApi, paymentApi
│   │   └── storage/               secureStorage (Keychain / Keystore)
│   ├── context/                   AuthContext, CartContext, WishlistContext
│   ├── hooks/                     useAsync, useProducts
│   ├── navigation/
│   │   ├── types.ts               param lists + typed screen props (no string routes)
│   │   ├── RootNavigator.tsx      root stack; SignIn/SignUp only exist for guests
│   │   ├── MainTabNavigator.tsx   Home / Cart / Favorites / Profile
│   │   ├── AdminNavigator.tsx     admin tabs, login screen for non-admins
│   │   └── AdminProductsNavigator.tsx
│   ├── components/                reusable UI: common/, product/, cart/, order/, icons/, toast/
│   ├── screens/<feature>/         one folder per feature, private parts in components/
│   │                              home, shop, product, cart, favorites, profile, auth,
│   │                              checkout, orders, admin (+ admin/products)
│   ├── utils/                     normalize, format, productFilters, errors
│   └── assets/                    images/ (logo, brand icon sources), data/banners.ts
├── __tests__/                     unit tests (filters, normalizers, formatters)
├── android/  ios/                 native projects
```

Conventions: `@/` imports resolve to `src/`; screens never call axios directly (use
`services/api`); styling is NativeWind `className` with theme colours; Prettier + ESLint
(`@react-native`) and strict TypeScript.

## Scripts

| Command | |
|---|---|
| `npm run typecheck` | `tsc --noEmit` |
| `npm run lint` / `lint:fix` | ESLint |
| `npm run format` / `format:check` | Prettier |
| `npm test` | Jest |
| `npm run validate` | typecheck + lint + test |

## What replaced the Expo libraries

| Expo | React Native CLI |
|---|---|
| expo-router (file routes) | React Navigation (native stack + bottom tabs) with typed param lists |
| @expo/vector-icons | @react-native-vector-icons/ionicons + /feather |
| expo-secure-store | react-native-keychain |
| expo-image-picker | react-native-image-picker |
| expo-constants / EXPO_PUBLIC_API_URL | `src/config/env.ts` + Metro host detection |
| babel-preset-expo / expo metro config | @react-native/babel-preset + metro-config (NativeWind kept) |

## Run in development

1. Start the Spring Boot API on your computer (port 8086, needs PostgreSQL — see
   [`backend/monolith/README.md`](../../../backend/monolith/README.md)). With `DEMO_MODE=true` it seeds the
   catalogue and a demo customer (`customer@shop.demo` / `Customer@123`).
2. In this folder:
   ```bash
   npm install
   npm start                 # Metro bundler (keep it running)
   npm run android           # in a second terminal: builds and opens the app on an emulator / phone
   ```
3. Phone over USB or emulator: run `npm run android:reverse` once so the app can reach Metro (8081)
   and the API (8086) on your computer. iOS simulator: `npm run ios` (run `pod install` in `ios/` first). A phone on the same Wi-Fi uses your computer's IP
   automatically.

To use the deployed backend from a debug build, set `API_URL_OVERRIDE = PRODUCTION_API_URL` in
`src/config/env.ts`.

## Build for release

```bash
npm run build:apk    # android/app/build/outputs/apk/release/app-release.apk
npm run build:aab    # android/app/build/outputs/bundle/release/app-release.aab  (Play Store)
```

Without an upload key, release builds are signed with the debug key: fine to install the APK on a
phone, but **Google Play only accepts builds signed with your own upload key**.

### Play Store upload key (once)

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore ~/keys/ecommerce-upload.keystore \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

Add to `~/.gradle/gradle.properties` (outside the project, never commit it):

```
MYAPP_UPLOAD_STORE_FILE=/Users/<you>/keys/ecommerce-upload.keystore
MYAPP_UPLOAD_KEY_ALIAS=upload
MYAPP_UPLOAD_STORE_PASSWORD=<password>
MYAPP_UPLOAD_KEY_PASSWORD=<password>
```

Then `npm run build:aab`. **Back up the keystore and passwords** - every future update must be signed
with the same key (with Play App Signing, Google can reset a lost upload key, but it takes time).

Before each Play Store upload, increase `versionCode` (and `versionName`) in `android/app/build.gradle`.

## iOS

```bash
cd ios && bundle install && bundle exec pod install && cd ..
npm run ios
```

Requires Xcode and CocoaPods. Icon fonts and the photo-library permission are already in `Info.plist`.

## Tests

`npm test` - unit tests for product filtering/sorting, API normalizers and formatters.
