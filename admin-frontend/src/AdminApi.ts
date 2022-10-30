import { DefaultApi } from "./api/api";
import globalAxios from 'axios';

export const AdminApi = new DefaultApi(undefined, 'http://localhost:8080/admin');
globalAxios.interceptors.response.use((response) => response, (error) => {
  const res = error.response;
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
