import globalAxios from 'axios';
import { DefaultApi } from "./api/api";
import { clearSavedAccessToken, getSavedAccessToken } from "./context/AuthContext";

const basePath = process.env.REACT_APP_BASE_URL || 'http://localhost:8080/admin'

export const ACCESS_TOKEN_HEADER = 'X-SCC-ACCESS-KEY'.toLowerCase();

export const AdminApi = new DefaultApi(undefined, basePath);

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
  if (res.status === 401 && getSavedAccessToken() != null) {
    clearSavedAccessToken();
  }
  if (res == null) {
    alert('알 수 없는 문제가 발생했습니다.');
    throw error;
  }
  const msg = res.data?.msg;
  if (msg == null) {
    alert('알 수 없는 문제가 발생했습니다.');
    throw error;
  }
  alert(msg);
  throw error;
});
