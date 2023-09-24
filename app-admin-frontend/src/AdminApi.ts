import globalAxios from 'axios';
import { DefaultApi, ChallengeApi } from "./api/api";
import { clearSavedAccessToken, getSavedAccessToken } from "./context/AuthContext";

const basePath = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/admin'

export const ACCESS_TOKEN_HEADER = 'X-SCC-ACCESS-KEY'.toLowerCase();

export const AdminApi = new DefaultApi(undefined, basePath); // deprecated; use AdminApis

export const AdminApis = {
  default: AdminApi,
  challenge: new ChallengeApi(undefined, basePath),
}

globalAxios.interceptors.request.use((request) => {
  if (!request.url!.includes("/admin/login")) {
    const savedAccessToken = getSavedAccessToken();
    if (savedAccessToken) {
      request.headers!['Authorization'] = `Bearer ${savedAccessToken}`;
    }
  }
  return request;
});

globalAxios.interceptors.response.use((response) => response, (error) => {
  const res = error.response;
  if (res.status === 401) {
    if (window.location.pathname !== '/login') {
      alert('잘못된 인증 정보입니다. 로그인 화면으로 이동합니다.');
      clearSavedAccessToken();
      window.location.pathname = '/login';
      return;
    }
  }
  if (res == null) {
    alert('알 수 없는 문제가 발생했습니다.');
    throw error;
  }
  if (typeof res.data === 'string') {
    alert(res.data);
    throw error;
  }
  alert('알 수 없는 문제가 발생했습니다.');
  throw error;
});
