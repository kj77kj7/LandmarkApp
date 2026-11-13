import Constants from "expo-constants";
import { Platform } from "react-native";

// Expo 개발 서버가 돌고 있는 PC의 IP를 가져옵니다 (휴대폰에서 같은 와이파이로 접속할 때 사용).
const debuggerHost = Constants.expoConfig?.hostUri; 
const localhost = debuggerHost?.split(":")[0];

// 웹이면 localhost, 앱이면 자동으로 찾은 IP를 사용합니다.
// IP를 찾지 못하면 아래 기본값을 씁니다. 자기 PC의 IP로 바꿔 주세요.
export const API_URL = Platform.OS === "web"
  ? "http://localhost:8080"
  : `http://${localhost || '192.168.219.113'}:8080`;

// 개발 중에만 연결 주소를 콘솔에 출력
if (__DEV__ && Platform.OS !== "web") {
  console.log(`[API] 서버 주소: ${API_URL}`);
}